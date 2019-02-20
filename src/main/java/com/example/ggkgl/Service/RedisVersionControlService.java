package com.example.ggkgl.Service;

import com.example.ggkgl.AssitClass.JSONHelper;
import com.example.ggkgl.Component.SpringUtil;
import com.example.ggkgl.Mapper.SpiderDataChangeRepository;
import com.example.ggkgl.Model.SpiderDataChangeEntity;
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
    private SpiderDataChangeRepository spiderDataChangeRepository;
    /**
     * 当数据版本变更导致调用resetIndex使记录对应下标发生改变时获取写锁,修改记录时获取读锁
     * 目的是使可以多个线程同时修改记录，但在同一时间只有一个线程对下标进行修改，
     * 并保障修改下标时没有线程在修改记录
     */
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

    /**
     * @param tableId 表的Id（即保存在mysql表META_ENTITY中的自增字段）
     * @return redis中用于更新目的mysql表的数据版本号
     */
    public String getCurrentVersion(int tableId){
        Jedis jedis = new Jedis();
        String versionCode = jedis.get("version"+tableId);
        if(versionCode == null){
            versionCode = this.setCurrentVersion(tableId);
        }
        return versionCode;
    }

    /**
     * @param tableId 表的Id（即保存在mysql表META_ENTITY中的自增字段）
     * @param originalData 原本的数据
     * @param currentData 新的数据
     * @return 版本是否发生了变化
     */
    @CacheEvict(value = "SpiderContrastResult",allEntries = true,condition = "#result")
    public boolean contrast(int tableId, Object originalData, Object currentData){
        boolean changed = (originalData == null && currentData!=null) ||
                (originalData != null && (currentData == null || !originalData.equals(currentData)));
        if(changed){
            String originalVersion = getCurrentVersion(tableId);
            String currentVersion = setCurrentVersion(tableId);
            if(originalData!=null && currentData!=null){
                List<Pair> detail = this.generateIndexChangeDetail(JSONHelper.jsonStr2MapList(originalData.toString())
                        ,JSONHelper.jsonStr2MapList(currentData.toString()));
                this.saveIndexChange(tableId,originalVersion,currentVersion,detail);
            }
        }
        return changed;
    }

    private void saveIndexChange(int tableId,String fromVersion,String toVersion,List<Pair> detail){
        Jedis jedis = new Jedis();
        Map<String,Object> data = new HashMap<>(2);
        data.put("versions",new Pair<>(fromVersion,toVersion));
        data.put("detail",detail);
        jedis.set("version_change"+tableId, JSONObject.fromObject(data).toString());
    }

    @SuppressWarnings("unchecked")
    public List<Pair> getIndexChangeDetail(int tableId,String fromVersion,String toVersion){
        Jedis jedis = new Jedis();
        Map<String,Object> data = JSONHelper.jsonStr2Map(jedis.get("version_change"+tableId));
        Pair versionInfo = (Pair)data.get("versions");
        if (!versionInfo.getKey().equals(fromVersion) || !versionInfo.getValue().equals(toVersion)){
            return null;
        }
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

    private synchronized String setCurrentVersion(int tableId){
        Jedis jedis = new Jedis();
        String versionCode = RandomStringUtils.randomAlphanumeric(10);
        jedis.set("version"+tableId, versionCode);
        return versionCode;
    }

    /**
     * 记录爬虫修改情况
     * @param tableId   mysql表id
     * @param modifyInfoList    修改列表,修改信息格式如下:
     *                          {"op":String ("DELETE"或"UPDATE",表示删除或修改),
     *                           "index":Integer (该数据在爬虫数据列表中的下标),
     *                           "id": Object (mysql记录的主键值，用于指明该爬虫信息用于更新哪条mysql记录),
     *                           "value":Map (爬虫数据修改后的值)
     *                          }
     * @return  发生变化的爬虫数据下标
     */
    @SuppressWarnings("unchecked")
    public Set<Integer> recordModifySpiderData(int tableId, List<HashMap> modifyInfoList){
        Set<Integer> result = new HashSet<>(modifyInfoList.size());
        this.lock.readLock().lock();
        try{
            for(HashMap map :modifyInfoList){
                int index = (int)map.get("index");
                //直接调用会导致缓存清除失败
                RedisVersionControlService redisVersionControlService = SpringUtil.getBean(RedisVersionControlService.class);
                if(redisVersionControlService.recordModifySpiderData(tableId,index,map)){
                    result.add(index);
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }finally {
            this.lock.readLock().unlock();
        }
        return result;
    }

    /**
     * Warning!!!
     * 不要在除recordModifySpiderData(int tableId, List<HashMap> modifyInfoList)这个方法外的其他地方调用这个函数
     * 因为这个函数没有加锁，把它设置为public仅仅是因为缓存需要
     */
    @SuppressWarnings("unchecked")
    @CacheEvict(value = "SpiderContrastResult",key = "#tableId+' '+#index",condition = "#result")
    public boolean recordModifySpiderData(int tableId,int index,HashMap modifyInfo){
        SpiderDataManagerService.OperatorCode op = SpiderDataManagerService.OperatorCode.valueOf(modifyInfo.get("op").toString());
        boolean changed = false;
        String watcher = tableId + "modify" + index;
        synchronized (watcher.intern()){
            switch (op){
                case DELETE:
                    this.deleteSpiderData(tableId, index);
                    changed = true;
                    break;
                case UPDATE:
                    changed = this.updateSpiderData(tableId, index,
                            modifyInfo.getOrDefault("id",null),(Map)modifyInfo.get("value"));
                    break;
                default:
                    break;
            }
        }
        return changed;
    }

    public SpiderDataChangeEntity getSpiderDataModifyInfo(int tableId, int index, boolean createIfAbsent){
        SpiderDataChangeEntity spiderDataChangeEntity =
                this.spiderDataChangeRepository.findByTableIdEqualsAndIndexEquals(tableId,index);
        if(createIfAbsent && spiderDataChangeEntity == null){
            spiderDataChangeEntity = new SpiderDataChangeEntity();
            spiderDataChangeEntity.setTableId(tableId);
            spiderDataChangeEntity.setIndex(index);
        }
        return spiderDataChangeEntity;
    }

    private void deleteSpiderData(int tableId,int index){
        SpiderDataChangeEntity spiderDataChangeEntity = this.getSpiderDataModifyInfo(tableId, index,true);
        spiderDataChangeEntity.setCurrentValue(null);
        spiderDataChangeEntity.setPrimaryValue(null);
        this.spiderDataChangeRepository.save(spiderDataChangeEntity);
    }

    @SuppressWarnings("unchecked")
    private boolean updateSpiderData(int tableId, int index,Object newMysqlId ,Map newValue){
        SpiderDataChangeEntity spiderDataChangeEntity = this.getSpiderDataModifyInfo(tableId, index,true);
        boolean changed = false;
        if(newMysqlId != null && !newMysqlId.equals(spiderDataChangeEntity.getPrimaryValue())){
            spiderDataChangeEntity.setPrimaryValue(newMysqlId);
            changed = true;
        }
        if(newValue != null){
            HashMap newValueField = new HashMap(newValue);
            if(newValueField.containsKey("index")){
                newValueField.remove("index");
            }
            Map currentValue = spiderDataChangeEntity.getCurrentValue();
            if(!newValueField.equals(currentValue)){
                spiderDataChangeEntity.setCurrentValue(newValueField);
                changed = true;
            }
        }
        if(changed){
            this.spiderDataChangeRepository.save(spiderDataChangeEntity);
        }
        return changed;
    }

    /**
     * 修改记录对应的数据下标变动
     * @return 失效的记录
     */
    public List<Pair> resetIndex(int tableId,List<Pair> indexChangeDetails){
        List<Pair> invalidChange = new ArrayList<>();
        this.lock.writeLock().lock();
        try{
            for(Pair change:indexChangeDetails){
                int oldIndex = (int)change.getKey();
                int newIndex = (int)change.getValue();
                SpiderDataChangeEntity entity = this.getSpiderDataModifyInfo(tableId,oldIndex,false);
                if (entity == null){
                    invalidChange.add(change);
                }else {
                    entity.setIndex(newIndex);
                    this.spiderDataChangeRepository.save(entity);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            this.lock.writeLock().unlock();
        }
        return invalidChange;
    }

}
