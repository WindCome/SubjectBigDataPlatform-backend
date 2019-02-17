package com.example.ggkgl.Service;

import com.example.ggkgl.AssitClass.JSONHelper;
import com.example.ggkgl.Mapper.SpiderDataChangeRepository;
import com.example.ggkgl.Model.SpiderDataChangeEntity;
import javafx.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
* 爬虫数据管理相关
 **/
@Service
public class SpiderDataManagerService {
    @Resource
    private SpiderDataChangeRepository spiderDataChangeRepository;

    @Resource
    private DataManagerService dataManagerService;

    /**
     * 当数据版本变更导致调用resetIndex使记录对应下标发生改变时获取写锁,修改记录时获取读锁
     * 目的是使可以多个线程同时修改记录，但在同一时间只有一个线程对下标进行修改，
     * 并保障修改下标时没有线程在修改记录
     */
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

    public enum OperatorCode{
        REDIRECT("REDIRECT"),UPDATE("UPDATE"),DELETE("DELETE");
        private String value;
        OperatorCode(String value){
            this.value = value;
        }
        @Override
        public String toString() {
            return this.value;
        }
    }

    /**
    *获取原始爬虫数据
     */
    public List<String> getJsonDataFromSpider(int tableId){
        String key = "upgrade"+tableId;
        Jedis jedis = new Jedis();
        long size = jedis.llen(key);
        List<String> result = new ArrayList<>((int)size);
        for(long i =0; i < size; i++){
            result.add(jedis.lindex(key,i));
        }
        return result;
    }

    /**
    * 获取原始爬虫数据列表
     */
    private List<HashMap> getDataListFromSpider(int tableId){
        List<String> jsonData = this.getJsonDataFromSpider(tableId);
        List<HashMap> result = new ArrayList<>(jsonData.size());
        for(String x : jsonData){
            result.add(JSONHelper.jsonStr2Map(x));
        }
        return result;
    }

    /**
     * @param tableId  mysql表id
     * @param data  数据
     * @param index 当data == null时使用，数据在列表中的下标
     * @return 经过修改的数据
     */
    @SuppressWarnings("unchecked")
    public HashMap getDataFromSpiderAfterModifying(int tableId, HashMap data, Integer index){
        HashMap map = data;
        if(map == null){
            List<HashMap> spiderData = this.getDataListFromSpider(tableId);
            if(spiderData == null || index == null || index >= spiderData.size()){
                return null;
            }
            map = spiderData.get(index);
        }
        SpiderDataChangeEntity spiderDataChangeEntity = this.getSpiderDataModifyInfo(tableId, index, false);
        if(spiderDataChangeEntity != null){
            Map currentValue = spiderDataChangeEntity.getCurrentValue();
            Object targetPrimaryValue = spiderDataChangeEntity.getPrimaryValue();
            if (currentValue == null && targetPrimaryValue == null) {
                //delete
                return null;
            }
            if (currentValue != null) {
                //update
                for (Object key : currentValue.keySet()) {
                    map.put(key, currentValue.get(key));
                }
            }
        }
        return map;
    }

    /**
     * 将redis中的一条记录与Mysql数据库进行对比
     * @param tableId  mysql表id
     * @param data  数据
     * @param index 当data == null时使用，数据在列表中的下标
     * @return 对比结果
     */
    @SuppressWarnings("unchecked")
    private HashMap getContrastResult(int tableId, HashMap data,Integer index){
        HashMap map = this.getDataFromSpiderAfterModifying(tableId,data,index);
        if(map == null){
            return null;
        }
        SpiderDataChangeEntity spiderDataChangeEntity =this.getSpiderDataModifyInfo(tableId,index,false);
        HashMap contrastResult = this.dataManagerService.contrast(tableId,map);
        if(spiderDataChangeEntity != null){
            Object targetPrimaryValue = spiderDataChangeEntity.getPrimaryValue();
            if (targetPrimaryValue != null){
                //redirect
                HashMap targetObject = this.dataManagerService.findByIdEquals(tableId,targetPrimaryValue);
                if(targetObject != null){
                    ArrayList dataList = (ArrayList) contrastResult.get("data");
                    dataList.add(0,targetObject);
                    if (targetObject.equals(map)){
                        contrastResult.put("status","same");
                    }else {
                        contrastResult.put("status","update");
                    }
                }
            }
        }
        return contrastResult;
    }

    /**
    * 获取爬虫数据和mysql数据库的对比结果，注意：使用的爬虫数据是经过更改记录修改的
     * @param tableId mysql表id
     * @param filter 筛选条件,格式如下:
     *               {"status":String (对比结果,"same"、"update"、"new"、"all"中的一种,默认为"all"),
     *                "page":Integer (页号，从0开始，默认为0),
     *                "size":Integer (页的大小,默认为20) ,
     *                "condition":{}  (匹配的字段)
     *                }
     * @return 符合条件的比对结果,格式如下:
     *              {"result":List<HashMap> (比对结果列表),
     *               "totalCount":Integer (符合条件的总数)
     *              }
     */
    @SuppressWarnings("unchecked")
    public HashMap<String,Object> getContrastResult(int tableId,@NotNull HashMap<String,Object> filter){
        int page = (int)filter.getOrDefault("page",0);
        int size = (int) filter.getOrDefault("size",20);
        String targetType = (String)filter.getOrDefault("status","all");
        HashMap condition = (HashMap) filter.getOrDefault("condition",null);
        List<HashMap> spiderData = this.getDataListFromSpider(tableId);
        List<HashMap> contrastResultList = new ArrayList<>(spiderData.size());
        int startIndex = page * size;
        int coincidentIndex = 0;
        for (int i = 0 ; i < spiderData.size() ; i++){
            HashMap map = this.getDataFromSpiderAfterModifying(tableId,spiderData.get(i),null);
            if(map == null){
                //爬虫数据有删除操作
                continue;
            }
            if(condition != null){
                //字段筛选
                boolean match = true;
                for(Object key:condition.keySet()){
                    if (!map.containsKey(key) || !map.get(key).equals(condition.get(key))){
                        match = false;
                        break;
                    }
                }
                if(!match){
                    continue;
                }
            }
            HashMap contrastResult = this.getContrastResult(tableId,map,i);
            Assert.notNull(contrastResult,"it's impossible");
            if(targetType.equals("all") || targetType.equals(contrastResult.get("status"))){
                if(coincidentIndex >= startIndex && coincidentIndex < (page+1)*size){
                    contrastResultList.add(contrastResult);
                }
                coincidentIndex++;
            }
        }
        HashMap<String,Object> result = new HashMap<>(2);
        result.put("result",contrastResultList);
        result.put("totalCount",coincidentIndex);
        return result;
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
     * @return  修改的爬虫数据下标
     */
    @SuppressWarnings("unchecked")
    public Set<Integer> recordModifySpiderData(int tableId, List<HashMap> modifyInfoList){
        Set<Integer> result = new HashSet<>(modifyInfoList.size());
        this.lock.readLock().lock();
        try{
            for(HashMap map :modifyInfoList){
                OperatorCode op = OperatorCode.valueOf(map.get("op").toString());
                int index = (int)map.get("index");
                String watcher = tableId + "modify" + index;
                synchronized (watcher.intern()){
                    switch (op){
                        case DELETE:
                            this.deleteSpiderData(tableId, index);
                            break;
                        case UPDATE:
                            this.updateSpiderData(tableId, index,
                                    map.getOrDefault("id",null),(Map)map.get("value"));
                            break;
                        default:
                            break;
                    }
                }
                result.add(index);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }finally {
            this.lock.readLock().unlock();
        }
        return result;
    }

    private SpiderDataChangeEntity getSpiderDataModifyInfo(int tableId, int index, boolean createIfAbsent){
        SpiderDataChangeEntity spiderDataChangeEntity =
                this.spiderDataChangeRepository.findByTableIdEqualsAndIndexEquals(tableId,index);
        if(createIfAbsent && spiderDataChangeEntity == null){
            spiderDataChangeEntity = new SpiderDataChangeEntity();
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

    private void updateSpiderData(int tableId, int index,Object newMysqlId ,Map newValue){
        SpiderDataChangeEntity spiderDataChangeEntity = this.getSpiderDataModifyInfo(tableId, index,true);
        if(newValue.containsKey("index")){
            newValue.remove("index");
        }
        if(newMysqlId != null){
            spiderDataChangeEntity.setPrimaryValue(newMysqlId);
        }
        spiderDataChangeEntity.setCurrentValue(newValue);
        this.spiderDataChangeRepository.save(spiderDataChangeEntity);
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
