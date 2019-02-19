package com.example.ggkgl.Service;

import com.example.ggkgl.AssitClass.JSONHelper;
import com.example.ggkgl.Model.SpiderDataChangeEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.util.*;

/**
* 爬虫数据管理相关
 **/
@Service
public class SpiderDataManagerService {
    @Resource
    private DataManagerService dataManagerService;

    @Resource
    private RedisVersionControlService redisVersionControlService;

    public enum OperatorCode{
        UPDATE("UPDATE"),DELETE("DELETE");
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
     * 获取原始爬虫数据列表大小
     */
    public int getSizeOfData(int tableId){
        return this.getDataListFromSpider(tableId).size();
    }

    /**
     * 获取指定下标的原始爬虫数据
     */
    private HashMap getDataListFromSpider(int tableId,int index){
        List<String> jsonData = this.getJsonDataFromSpider(tableId);
        if(jsonData == null || index >= jsonData.size()){
            return null;
        }
        return JSONHelper.jsonStr2Map(jsonData.get(index));
    }

    /**
     * @param data  数据
     * @param modifyInfo 修改信息
     * @return 经过修改的数据
     */
    @SuppressWarnings("unchecked")
    private HashMap getDataFromSpiderAfterModifying(HashMap data, SpiderDataChangeEntity modifyInfo){
        if(data == null){
            return null;
        }
        if(modifyInfo != null){
            Map currentValue = modifyInfo.getCurrentValue();
            Object targetPrimaryValue = modifyInfo.getPrimaryValue();
            if (currentValue == null && targetPrimaryValue == null) {
                //delete
                return null;
            }
            if (currentValue != null) {
                //update
                for (Object key : currentValue.keySet()) {
                    data.put(key, currentValue.get(key));
                }
            }
        }
        return data;
    }

    /**
     * 将redis中的一条记录与Mysql数据库进行对比(在版本变化时删除所有缓存，
     *                              在更改相关记录时把相关记录缓存清除)
     * @param tableId  mysql表id
     * @param index   redis数据下标
     * @return 对比结果
     */
    @SuppressWarnings("unchecked")
    @Cacheable(value = "SpiderContrastResult",key = "#tableId+' '+#index")
    public HashMap getContrastResult(int tableId, int index){
        System.out.println("====step in ==== "+tableId+" "+index);
        HashMap data = this.getDataListFromSpider(tableId,index);
        SpiderDataChangeEntity modifyInfo = this.redisVersionControlService.getSpiderDataModifyInfo(tableId,index,false);
        data = this.getDataFromSpiderAfterModifying(data,modifyInfo);
        return this.getContrastResult(tableId,data,modifyInfo);
    }

    /**
     * 将redis中的一条记录与Mysql数据库进行对比
     * @param tableId  mysql表id
     * @param data  经过修改和删除的redis数据
     * @param modifyInfo 修改信息
     * @return 对比结果
     */
    @SuppressWarnings("unchecked")
    private HashMap getContrastResult(int tableId, HashMap data,SpiderDataChangeEntity modifyInfo){
        if(data == null){
            return null;
        }
        HashMap contrastResult = this.dataManagerService.contrast(tableId,data);
        if(modifyInfo != null){
            Object targetPrimaryValue = modifyInfo.getPrimaryValue();
            if (targetPrimaryValue != null){
                //redirect
                HashMap targetObject = this.dataManagerService.findByIdEquals(tableId,targetPrimaryValue);
                if(targetObject != null){
                    ArrayList dataList = (ArrayList) contrastResult.get("data");
                    dataList.add(0,targetObject);
                    if (targetObject.equals(data)){
                        contrastResult.put("status","same");
                    }else {
                        contrastResult.put("status","update");
                    }
                }
            }
        }
        return contrastResult;
    }
}
