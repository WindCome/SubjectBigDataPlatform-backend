package com.example.ggkgl.Service;

import com.example.ggkgl.AssitClass.JSONHelper;
import com.example.ggkgl.Mapper.SpiderDataChangeRepository;
import com.example.ggkgl.Model.SpiderDataChangeEntity;
import javafx.util.Pair;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
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
    public List<HashMap> getDataListFromSpider(int tableId){
        List<String> jsonData = this.getJsonDataFromSpider(tableId);
        List<HashMap> result = new ArrayList<>(jsonData.size());
        for(String x : jsonData){
            result.add(JSONHelper.jsonStr2Map(x));
        }
        return result;
    }

    public List<HashMap> getContrastResult(int tableId){
        return this.getContrastResult(tableId,null,null);
    }

    /**
    * 获取爬虫数据和mysql数据库的对比结果，注意：使用的爬虫数据是经过更改记录修改的
     */
    @SuppressWarnings("unchecked")
    public List<HashMap> getContrastResult(int tableId,Integer page,Integer size){
        List<HashMap> spiderData = this.getDataListFromSpider(tableId);
        List<HashMap> result = new ArrayList<>(spiderData.size());
        int startIndex = 0;
        int counter = spiderData.size();
        if(page != null && size != null){
            startIndex = page * size;
        }
        for (int i = startIndex;i<counter;i++){
            HashMap map = spiderData.get(i);
            SpiderDataChangeEntity spiderDataChangeEntity =
                    this.spiderDataChangeRepository.findByTableIdEqualsAndIndexEquals(tableId, i);
            if(spiderDataChangeEntity == null){
                HashMap contrastResult = this.dataManagerService.contrast(tableId,map);
                result.add(contrastResult);
            }else {
                Map currentValue = spiderDataChangeEntity.getCurrentValue();
                Object targetPrimaryValue = spiderDataChangeEntity.getPrimaryValue();
                if(currentValue == null && targetPrimaryValue == null){
                    //delete
                    continue;
                }
                if(currentValue !=null){
                    //update
                    for (Object key : currentValue.keySet()){
                        map.put(key,currentValue.get(key));
                    }
                }
                HashMap contrastResult = this.dataManagerService.contrast(tableId,map);
                if (targetPrimaryValue != null){
                    //redirect
                    HashMap targetObject = this.dataManagerService.findByIdEquals(tableId,targetPrimaryValue);
                    if(targetObject != null){
                        ArrayList data = (ArrayList) contrastResult.get("data");
                        data.add(0,targetObject);
                        if (targetObject.equals(map)){
                            contrastResult.put("status","same");
                        }else {
                            contrastResult.put("status","update");
                        }
                    }
                }
                result.add(contrastResult);
            }
        }
        return result;
    }

    public void modifySpiderData(int tableId, List<HashMap> modifyInfoList){
        this.lock.readLock().lock();
        try{
            for(HashMap map :modifyInfoList){
                OperatorCode op = OperatorCode.valueOf(map.get("op").toString());
                int index = (int)map.get("index");
                String watcher = "modify"+index;
                synchronized (watcher.intern()){
                    switch (op){
                        case DELETE:
                            this.deleteSpiderData(tableId, index);
                            break;
                        case UPDATE:
                            this.updateSpiderData(tableId, index,(Map)map.get("value"));
                            break;
                        case REDIRECT:
                            this.redirectSpiderData(tableId, index,map.get("id"));
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }finally {
            this.lock.readLock().unlock();
        }
    }

    private SpiderDataChangeEntity getSpiderDataOrSetIndex(int tableId, int index){
        SpiderDataChangeEntity spiderDataChangeEntity =
                this.spiderDataChangeRepository.findByTableIdEqualsAndIndexEquals(tableId,index);
        if(spiderDataChangeEntity == null){
            spiderDataChangeEntity = new SpiderDataChangeEntity();
            spiderDataChangeEntity.setIndex(index);
        }
        return spiderDataChangeEntity;
    }

    private void deleteSpiderData(int tableId,int index){
        SpiderDataChangeEntity spiderDataChangeEntity = this.getSpiderDataOrSetIndex(tableId, index);
        spiderDataChangeEntity.setCurrentValue(null);
        spiderDataChangeEntity.setPrimaryValue(null);
        this.spiderDataChangeRepository.save(spiderDataChangeEntity);
    }

    private void updateSpiderData(int tableId, int index ,Map newValue){
        SpiderDataChangeEntity spiderDataChangeEntity = this.getSpiderDataOrSetIndex(tableId, index);
        if(newValue.containsKey("index")){
            newValue.remove("index");
        }
        spiderDataChangeEntity.setCurrentValue(newValue);
        this.spiderDataChangeRepository.save(spiderDataChangeEntity);
    }

    private void redirectSpiderData(int tableId, int index,Object newMysqlId){
        SpiderDataChangeEntity spiderDataChangeEntity = this.getSpiderDataOrSetIndex(tableId, index);
        spiderDataChangeEntity.setPrimaryValue(newMysqlId);
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
                SpiderDataChangeEntity entity =
                        this.spiderDataChangeRepository.findByTableIdEqualsAndIndexEquals(tableId,oldIndex);
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
