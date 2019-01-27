package com.example.ggkgl.Service;

import com.example.ggkgl.Mapper.GreatMapper;
import javafx.util.Pair;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.*;

/**
*  数据增删查改相关
**/
@Service
public class DataManagerService {
    @Resource
    private GreatMapper greatMapper;

    @Resource
    private RedisVersionControlService redisVersionControlService;

    @Resource
    private TableConfigService tableConfigService;

    /**
     * @param tableId 表的Id（即保存在META_ENTITY中的自增字段）
     * @return {key:版本号,value:数据列表}
     */
    public Pair<String,List<HashMap>> getDataFromSpider(int tableId){
        List<HashMap> result = new ArrayList<>();
        Jedis jedis = new Jedis();
        String jsonStr=jedis.get("upgrade"+tableId);
        JSONArray jsonArray= JSONArray.fromObject(jsonStr);
        for (Object aJsonArray : jsonArray) {
            JSONObject item = JSONObject.fromObject(aJsonArray);
            Iterator iterator = item.keys();
            HashMap<String, Object> itemMap = new HashMap<>();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                itemMap.put(key, item.get(key));
            }
            result.add(itemMap);
        }
        return new Pair<>(this.redisVersionControlService.getCurrentVersion(tableId),result);
    }

    /**
     * @param tableId 表的Id（即保存在META_ENTITY中的自增字段）
     * @param data 要对比的数据Map
     * @param matchKeys 匹配的字段名
     * @return 对比的结果{"oriData":参数中的data,"status":数据对比结果,"data":相关的数据,按匹配程度从大到小排列}
     */
    public HashMap<String,Object> contrast(int tableId, @NotNull HashMap data, String[] matchKeys){
        HashMap<String,Object> resultMap=new HashMap<>();
        String tableName = tableConfigService.getTableNameById(tableId);
        String[] columnNames = this.sortColumnByDiversityFactor(tableId);
        List<String> matchKeyList = new ArrayList<>(Arrays.asList(matchKeys));
        for(String c:columnNames){
            if(!matchKeyList.contains(c)){
                matchKeyList.add(c);
            }
        }
        matchKeyList.retainAll(data.keySet());
        String mainKey = matchKeyList.get(0);
        if(!data.containsKey(mainKey)){
            System.out.println("数据中没有字段: "+mainKey);
            return resultMap;
        }
        List<HashMap> mainKeyMatchMaps=greatMapper.freeInspect(tableName,mainKey,data.get(mainKey).toString());
        if(mainKeyMatchMaps.size()==0){
            resultMap.put("status","new");
        }
        else {
            mainKeyMatchMaps.sort((o1, o2) -> {
                for(String key : matchKeyList){
                    Object targetValue = data.get(key);
                    boolean o1Match = o1.get(key).equals(targetValue);
                    boolean o2Match = o2.get(key).equals(targetValue);
                    if(o2Match && !o1Match){
                        return 1;
                    }
                    else if (!o2Match && o1Match){
                        return -1;
                    }
                }
                return 0;
            });
            boolean matchFirst = true;
            HashMap first = mainKeyMatchMaps.get(0);
            for(Object key :data.keySet()){
                if(!data.get(key).equals(first.get(key))){
                    matchFirst = false;
                    break;
                }
            }
            if(matchFirst){
                resultMap.put("status","same");
            }
            else {
                resultMap.put("status","update");
                resultMap.put("data",mainKeyMatchMaps);
            }
        }
        if(!resultMap.containsKey("data")){
            resultMap.put("data",new LinkedHashMap<>(0));
        }
        resultMap.put("oriData",data);
        return resultMap;
    }

    /**
     * 将表的字段按差异度从大到小排序
     * @param tableId 表的Id（即保存在META_ENTITY中的自增字段）
     * @return 字段名称
     */
    private String[] sortColumnByDiversityFactor(int tableId){
        String tableName = tableConfigService.getTableNameById(tableId);
        String[] columnNames = this.tableConfigService.getColumnNamesOfTable(tableId);
        int sizeOfTable = this.greatMapper.getSize(tableName);
        if(sizeOfTable == 0 || columnNames.length == 0){
            return columnNames;
        }
        Map<String,Float> columnToDiversityFactorMap = new HashMap<>(columnNames.length);
        for(String c:columnNames){
            float factor = this.greatMapper.countDistinctColumn(tableName,c)/sizeOfTable;
            columnToDiversityFactorMap.put(c,factor);
        }
        columnToDiversityFactorMap = this.sortMapByValue(columnToDiversityFactorMap);
        return columnToDiversityFactorMap.keySet().toArray(new String[0]);
    }

    /**
     * Map按value进行排序
     */
    private Map<String, Float> sortMapByValue(Map<String, Float> oriMap) {
        if (oriMap == null || oriMap.isEmpty()) {
            return new HashMap<>(0);
        }
        Map<String, Float> sortedMap = new LinkedHashMap<>();
        List<Map.Entry<String, Float>> entryList = new ArrayList<>(oriMap.entrySet());
        entryList.sort((o1, o2) -> (int) (o2.getValue() - o1.getValue()));

        for (Map.Entry<String, Float> tmpEntry : entryList) {
            sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
        }
        return sortedMap;
    }
}
