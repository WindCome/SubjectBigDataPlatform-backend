package com.example.ggkgl.Service;

import com.example.ggkgl.AssitClass.ExceptionHelper;
import com.example.ggkgl.AssitClass.JSONHelper;
import com.example.ggkgl.Component.SpringUtil;
import com.example.ggkgl.Mapper.RedisDataChangeRepository;
import com.example.ggkgl.Model.RedisDataChangeEntity;
import javafx.util.Pair;
import net.sf.json.JSONObject;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
* Redis数据版本控制相关
 **/
@Service
public class RedisVersionControlService {
    @Resource
    private RedisDataChangeRepository redisDataChangeRepository;
    /**
     * 当数据版本变更导致调用resetIndex使记录对应下标发生改变时获取写锁,修改记录时获取读锁
     * 目的是使可以多个线程同时修改记录，但在同一时间只有一个线程对下标进行修改，
     * 并保障修改下标时没有线程在修改记录
     */
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

    /**
     * @param redisKey redis键
     * @return redis中用于更新目的mysql表的数据版本号
     */
    public String getCurrentVersion(String redisKey){
        Jedis jedis = new Jedis();
        String versionCode = jedis.get("version"+redisKey);
        if(versionCode == null){
            versionCode = this.setCurrentVersion(redisKey);
        }
        return versionCode;
    }

    /**
     * @param redisKey redis键
     * @param originalData 原本的数据
     * @param currentData 新的数据
     * @return 版本是否发生了变化
     */
    @CacheEvict(value = "RedisContrastResult",allEntries = true,condition = "#originalData!=null&&#result")
    public boolean contrast(String redisKey, Object originalData, Object currentData){
        boolean changed = (originalData == null && currentData!=null) ||
                (originalData != null && (currentData == null || !originalData.equals(currentData)));
        if(changed){
            String originalVersion = getCurrentVersion(redisKey);
            String currentVersion = setCurrentVersion(redisKey);
            if(originalData!=null && currentData!=null){
                List<Pair> detail = this.generateIndexChangeDetail(JSONHelper.jsonStr2MapList(originalData.toString())
                        ,JSONHelper.jsonStr2MapList(currentData.toString()));
                this.saveIndexChange(redisKey,originalVersion,currentVersion,detail);
            }
        }
        return changed;
    }

    private void saveIndexChange(String redisKey,String fromVersion,String toVersion,List<Pair> detail){
        Jedis jedis = new Jedis();
        Map<String,Object> data = new HashMap<>(2);
        data.put("versions",new Pair<>(fromVersion,toVersion));
        data.put("detail",detail);
        jedis.set("version_change"+redisKey, JSONObject.fromObject(data).toString());
    }

    @SuppressWarnings("unchecked")
    public List<Pair> getIndexChangeDetail(String redisKey,String fromVersion,String toVersion){
        Jedis jedis = new Jedis();
        Map<String,Object> data = JSONHelper.jsonStr2Map(jedis.get("version_change"+redisKey));
        JSONObject versionInfo = (JSONObject)data.get("versions");
        if (!versionInfo.getString("key").equals(fromVersion) || !versionInfo.getString("value").equals(toVersion)){
            return null;
        }
        System.out.println("detail "+data.get("detail"));
        return (List<Pair>)data.get("detail");
    }

    /**
    * 记录未改动数据的位置变化
     */
    private List<Pair> generateIndexChangeDetail(List<HashMap> originalData,List<HashMap> currentData){
        if(originalData == null || currentData == null){
            return null;
        }
        List<Pair> indexChange = new ArrayList<>();
        for(int i = 0;i < originalData.size() ; i++){
            HashMap map = originalData.get(i);
            int newIndex = currentData.indexOf(map);
            if(newIndex != -1){
                Pair change = new Pair<>(i,newIndex);
                indexChange.add(change);
            }
        }
        return indexChange;
    }

    private synchronized String setCurrentVersion(String redisKey){
        Jedis jedis = new Jedis();
        String versionCode = RandomStringUtils.randomAlphanumeric(10);
        jedis.set("version"+redisKey, versionCode);
        return versionCode;
    }

    /**
     * 记录redis修改情况
     * @param redisKey  redis键
     * @param modifyInfo    修改信息,格式如下:
     *                          {"op":String ("DELETE"或"UPDATE",表示删除或修改),
     *                           "index":Integer (该数据在爬虫数据列表中的下标),
     *                           "id": Object (mysql记录的主键值，用于指明该爬虫信息用于更新哪条mysql记录),
     *                           "value":Map (爬虫数据修改后的值)
     *                          }
     * @return  {Boolean(是否修改成功):
     *              Object(修改成功时放入true\false表示数据是否发生改变，修改失败时放入出错信息)}
     */
    @SuppressWarnings("unchecked")
    public Pair<Boolean,Object> recordModifyRedisData(String redisKey, HashMap modifyInfo){
        if(modifyInfo == null){
            return new Pair<>(false,"修改信息为空");
        }
        Pair<Boolean,Object> result;
        this.lock.readLock().lock();
        try{
            int index = (int)modifyInfo.get("index");
            //直接调用会导致缓存清除失败
            RedisVersionControlService redisVersionControlService = SpringUtil.getBean(RedisVersionControlService.class);
            boolean changed = redisVersionControlService.recordModifyRedisData(redisKey,index,modifyInfo);
            if(changed){
                result = new Pair<>(true,true);
            }else{
                result = new Pair<>(true,false);
            }
        }
        catch (Exception e){
            result = new Pair<>(false,ExceptionHelper.getExceptionAllInfo(e));
        }finally {
            this.lock.readLock().unlock();
        }
        return result;
    }

    /**
     * Warning!!!
     * 不要在除recordModifyRedisData(int tableId, List<HashMap> modifyInfoList)这个方法外的其他地方调用这个函数
     * 因为这个函数没有加锁，把它设置为public仅仅是因为缓存需要
     */
    @SuppressWarnings("unchecked")
    @CacheEvict(value = "RedisContrastResult",key = "#redisKey+' '+#index",condition = "#result")
    public boolean recordModifyRedisData(String redisKey, int index, HashMap modifyInfo){
        RedisDataManagerService.OperatorCode op = RedisDataManagerService.OperatorCode.valueOf(modifyInfo.get("op").toString());
        boolean changed = false;
        String watcher = redisKey + "modify" + index;
        synchronized (watcher.intern()){
            switch (op){
                case DELETE:
                    this.deleteRedisData(redisKey, index);
                    changed = true;
                    break;
                case UPDATE:
                    changed = this.updateRedisData(redisKey, index,
                            modifyInfo.getOrDefault("id",null),(Map)modifyInfo.get("value"));
                    break;
                case RESET:
                    changed = this.resetRedisData(redisKey, index);
                default:
                    break;
            }
        }
        return changed;
    }

    public RedisDataChangeEntity getRedisDataModifyInfo(String redisKey, int index, boolean createIfAbsent){
        RedisDataChangeEntity redisDataChangeEntity =
                this.redisDataChangeRepository.findByRedisKeyEqualsAndIndexEquals(redisKey,index);
        if(createIfAbsent && redisDataChangeEntity == null){
            redisDataChangeEntity = new RedisDataChangeEntity();
            redisDataChangeEntity.setRedisKey(redisKey);
            redisDataChangeEntity.setIndex(index);
        }
        return redisDataChangeEntity;
    }

    private boolean resetRedisData(String redisKey, int index){
        RedisDataChangeEntity redisDataChangeEntity = this.getRedisDataModifyInfo(redisKey, index, false);
        if(redisDataChangeEntity == null || !redisDataChangeEntity.isDelete()){
            return false;
        }
        redisDataChangeEntity.setDelete(false);
        this.redisDataChangeRepository.save(redisDataChangeEntity);
        return true;
    }

    private void deleteRedisData(String redisKey, int index){
        RedisDataChangeEntity redisDataChangeEntity = this.getRedisDataModifyInfo(redisKey, index,true);
        redisDataChangeEntity.setDelete(true);
        this.redisDataChangeRepository.save(redisDataChangeEntity);
    }

    @SuppressWarnings("unchecked")
    private boolean updateRedisData(String redisKey, int index, Object newMysqlId , Map newValue){
        RedisDataChangeEntity redisDataChangeEntity = this.getRedisDataModifyInfo(redisKey, index,true);
        boolean changed = false;
        if(newMysqlId != null && !newMysqlId.equals(redisDataChangeEntity.getPrimaryValue())){
            redisDataChangeEntity.setPrimaryValue(newMysqlId);
            changed = true;
        }
        if(newValue != null){
            HashMap newValueField = new HashMap(newValue);
            if(newValueField.containsKey("index")){
                newValueField.remove("index");
            }
            Map currentValue = redisDataChangeEntity.getCurrentValue();
            if(!newValueField.equals(currentValue)){
                redisDataChangeEntity.setCurrentValue(newValueField);
                changed = true;
            }
        }
        if(changed){
            this.redisDataChangeRepository.save(redisDataChangeEntity);
        }
        return changed;
    }

    /**
     * 修改记录对应的数据下标变动
     * @return 失效的记录
     */
    public List<Pair> resetIndex(String redisKey,List<Pair> indexChangeDetails){
        if(indexChangeDetails == null || indexChangeDetails.size() == 0){
            return new ArrayList<>(0);
        }
        List<Pair> invalidChange = new ArrayList<>();
        List<Long> indexToChange = new ArrayList<>(indexChangeDetails.size());
        this.lock.writeLock().lock();
        try{
            for(Pair change:indexChangeDetails){
                int oldIndex = (int)change.getKey();
                int newIndex = (int)change.getValue();
                RedisDataChangeEntity entity = this.getRedisDataModifyInfo(redisKey,oldIndex,false);
                if (entity == null){
                    invalidChange.add(change);
                }else {
                    entity.setIndex(newIndex);
                    this.redisDataChangeRepository.save(entity);
                }
                indexToChange.add((Long)change.getKey());
            }
            List<Long> historyList = this.redisDataChangeRepository.findIdAll();
            historyList.retainAll(indexToChange);
            for(Long id:historyList){
                this.redisDataChangeRepository.deleteById(id);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            this.lock.writeLock().unlock();
        }
        return invalidChange;
    }

}
