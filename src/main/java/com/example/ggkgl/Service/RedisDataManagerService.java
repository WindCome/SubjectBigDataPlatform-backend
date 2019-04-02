package com.example.ggkgl.Service;

import com.example.ggkgl.AssitClass.JSONHelper;
import com.example.ggkgl.Model.RedisDataChangeEntity;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.util.*;

/**
* Redis数据管理相关
 **/
@Service
public class RedisDataManagerService {
    @Resource
    private MysqlDataManagerService mysqlDataManagerService;

    @Resource
    private RedisVersionControlService redisVersionControlService;

    @Resource
    private TableConfigService tableConfigService;

    private final Map<String,Integer> redisKey2MysqlIdMap;

    private final String configKey = "key2mysql";

    public RedisDataManagerService() {
        final int tableCount = 60;          //保留60个公共库的数量
        HashMap<String,Integer> map = new HashMap<>(tableCount);
        for(int i=1;i<=tableCount;i++){
            map.put(this.getSpiderDataRedisKey(i),i);
        }
        this.redisKey2MysqlIdMap = Collections.unmodifiableMap(map);
    }

    public enum OperatorCode{
        UPDATE("UPDATE"),DELETE("DELETE"),RESET("RESET");
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
    * 查询存放用于更新指定id的Mysql表的redis键值
     */
    public static String getSpiderDataRedisKey(int tableId){
        return "upgrade"+tableId;
    }

    /**
     * 查询指定redis键中的数据用于更新的Mysql表id
     */
    public Integer getTargetMysqlTableId(String redisKey){
        if(this.redisKey2MysqlIdMap.containsKey(redisKey)){
            return this.redisKey2MysqlIdMap.get(redisKey);
        }
        Jedis jedis = new Jedis();
        HashMap keyMap = JSONHelper.jsonStr2Map(jedis.get(configKey));
        if(keyMap.containsKey(redisKey)){
            return (int)keyMap.get(redisKey);
        }
        return null;
    }

    /**
     * 设置redis键对应的Mysql表id
     */
    @SuppressWarnings("unchecked")
    private boolean setTargetMysqlTableId(String redisKey,int tableId){
        if(redisKey == null || this.redisKey2MysqlIdMap.containsKey(redisKey)){
            return false;
        }
        synchronized (this.redisKey2MysqlIdMap){
            Jedis jedis = new Jedis();
            HashMap keyMap = JSONHelper.jsonStr2Map(jedis.get(configKey));
            if(keyMap.containsKey(redisKey)){
                return false;
            }else{
                keyMap.put(redisKey,tableId);
                jedis.set(this.configKey,JSONHelper.map2Json(keyMap));
            }
        }
        return true;
    }

    public String generateNewRedisKeyForMysqlTable(int tableId){
        String result = null;
        while (!this.setTargetMysqlTableId(result,tableId)){
            result = "upgrade"+ RandomStringUtils.randomAlphanumeric(10);
        }
        return result;
    }

    /**
    *获取原始JSON格式数据
     */
    public List<String> getJsonDataList(String redisKey){
        Jedis jedis = new Jedis();
        long size = jedis.llen(redisKey);
        List<String> result = new ArrayList<>((int)size);
        for(long i =0; i < size; i++){
            result.add(jedis.lindex(redisKey,i));
        }
        return result;
    }

    /**
    * 获取原始数据列表
     */
    private List<HashMap> getDataList(String redisKey){
        List<String> jsonData = this.getJsonDataList(redisKey);
        List<HashMap> result = new ArrayList<>(jsonData.size());
        for(String x : jsonData){
            result.add(JSONHelper.jsonStr2Map(x));
        }
        return result;
    }

    /**
     * 获取指定下标的redis数据
     */
    private HashMap getData(String redisKey, int index){
        List<String> jsonData = this.getJsonDataList(redisKey);
        if(jsonData == null || index >= jsonData.size()){
            return null;
        }
        return JSONHelper.jsonStr2Map(jsonData.get(index));
    }

    /**
     * 获取原始数据列表大小
     */
    public int getSizeOfData(String redisKey){
        return this.getDataList(redisKey).size();
    }

    /**
     * @param data  数据
     * @param modifyInfo 修改信息
     * @return 经过修改的数据
     */
    @SuppressWarnings("unchecked")
    private HashMap getDataAfterModifying(HashMap data, RedisDataChangeEntity modifyInfo){
        if(data == null){
            return null;
        }else if(modifyInfo == null){
            return data;
        }
        Map currentValue = modifyInfo.getCurrentValue();
        if (currentValue != null) {
            //update
            for (Object key : currentValue.keySet()) {
                data.put(key, currentValue.get(key));
            }
        }
        return data;
    }

    /**
     * 将redis中的一条记录与Mysql数据库进行对比(在版本变化时删除所有缓存，
     *                              在更改相关记录时把相关记录缓存清除)
     * @param redisKey  redis键
     * @param index   redis数据下标
     * @return 对比结果
     */
    @SuppressWarnings("unchecked")
    @Cacheable(value = "RedisContrastResult",key = "#redisKey+' '+#index")
    public HashMap getContrastResult(String redisKey, int index){
        HashMap data = this.getData(redisKey,index);
        RedisDataChangeEntity modifyInfo = this.redisVersionControlService.getRedisDataModifyInfo(redisKey,index,false);
        Integer tableId = this.getTargetMysqlTableId(redisKey);
        if(tableId == null){
            return new HashMap(0);
        }
        return this.getContrastResult(tableId,data,modifyInfo);
    }

    /**
     * 将redis中的一条记录与Mysql数据库进行对比
     * @param tableId  mysql表id
     * @param oriData  未经修改的redis数据
     * @param modifyInfo 修改信息
     * @return 对比结果
     */
    @SuppressWarnings("unchecked")
    private HashMap getContrastResult(int tableId, HashMap oriData,RedisDataChangeEntity modifyInfo){
        if(oriData == null){
            return null;
        }
        HashMap data = this.getDataAfterModifying(oriData,modifyInfo);
        if(modifyInfo !=null && modifyInfo.isDelete()){
            //delete
            HashMap contrastResult = new HashMap(3);
            contrastResult.put("status","delete");
            contrastResult.put("data",new ArrayList<>(0));
            contrastResult.put("oriData",oriData);
            return contrastResult;
        }
        HashMap contrastResult = this.mysqlDataManagerService.contrast(tableId,data);
        if(modifyInfo != null){
            Object targetPrimaryValue = modifyInfo.getPrimaryValue();
            if (targetPrimaryValue != null){
                //redirect
                String primaryKey = this.tableConfigService.getPrimaryKeyByTableId(tableId);
                List<HashMap> similarData = (List<HashMap>)contrastResult.get("data");
                HashMap targetObject = null;
                for(HashMap map :similarData){
                    if(map.containsKey(primaryKey) && map.get(primaryKey).equals(targetPrimaryValue)){
                        targetObject = map;
                        similarData.remove(map);
                        break;
                    }
                }
                if(targetObject == null){
                    targetObject = this.mysqlDataManagerService.findByIdEquals(tableId,targetPrimaryValue);
                }
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
