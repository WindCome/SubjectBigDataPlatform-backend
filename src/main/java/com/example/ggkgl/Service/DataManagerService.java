package com.example.ggkgl.Service;

import com.example.ggkgl.AssitClass.JSONHelper;
import com.example.ggkgl.Mapper.GreatMapper;
import javafx.util.Pair;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
*  数据增删查改相关
**/
@Service
public class DataManagerService {
    public enum OperatorCode{
        NEW("new"),UPGRADE("upgrade"),DELETE("delete"),SAME("same");
        private String value;
        OperatorCode(String value){
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }
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
        for (Object x : jsonArray) {
            JSONObject item = JSONObject.fromObject(x);
            result.add(JSONHelper.json2Map(item));
        }
        return new Pair<>(this.redisVersionControlService.getCurrentVersion(tableId),result);
    }

    /**
     * 将数据与mysql数据库中的数据进行对比，字段对比顺序为主键、配置中的matchKey、最后是差异度从大到小的字段
     * @param tableId 表的Id（即保存在META_ENTITY中的自增字段）
     * @param data 要对比的数据Map
     * @return 对比的结果{"oriData":参数中的data,"status":数据对比结果,"data":相关的数据,按匹配程度从大到小排列}
     */
    public HashMap<String,Object> contrast(int tableId, @NotNull HashMap data){
        HashMap<String,Object> resultMap=new HashMap<>();
        String tableName = tableConfigService.getTableNameById(tableId);
        String primaryKey = this.tableConfigService.getPrimaryKeyByTableId(tableId);
        String[] matchKeys = this.tableConfigService.getMatchKeyField(tableId);
        String[] columnNames = this.sortColumnByDiversityFactor(tableId);
        List<String> matchKeyList = new ArrayList<>(Arrays.asList(matchKeys));
        matchKeyList.add(0,primaryKey);
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

    /**
     * @param version  应用变更的mysql数据版本,在aop中使用了该参数
     * @param tableId   mysql表id
     * @param indexOfRedisData  redis数据索引
     * @param alterInfo     修改信息
     * @return      保存结果详细信息{success:true/false(全部数据插入失败时才为false)
     *                                 ,details:[{}]}
     */
    @SuppressWarnings("unchecked")
    public HashMap<String,Object> saveData(String version,int tableId, List<Integer> indexOfRedisData, List<HashMap> alterInfo){
        Pair<String,List<HashMap>> redisData = this.getDataFromSpider(tableId);
        List<HashMap> data = redisData.getValue();
        List<HashMap<String,Object>> contrastResults = new ArrayList<>(data.size());
        for(HashMap x:data){
            contrastResults.add(this.contrast(tableId,x));
        }
        for(int index:indexOfRedisData){
            HashMap<String,Object> contrastResult = contrastResults.get(index);
            OperatorCode opCode = OperatorCode.valueOf(contrastResult.get("status").toString());
            HashMap alter = this.searchMapByKeyAndValue(alterInfo,"index",index);
            switch (opCode){
                case NEW:
                    this.redis2MysqlInsert(tableId,(HashMap<String,Object>)contrastResult.get("oriData"),alter);
                    break;
                case UPGRADE:
                    this.redis2MysqlUpdate(tableId,(HashMap<String,Object>)contrastResult.get("oriData"),alter);
                    break;
                case DELETE:
                case SAME:
                default:
                    break;
            }
        }
        return null;
    }

    /**
     * 工具方法，查找列表中键和值相应的字典
     */
    private HashMap searchMapByKeyAndValue(List<HashMap> maps,String key,Object value){
        for(HashMap map:maps){
            if(map.containsKey(key)&&map.get(key).equals(value)){
                return map;
            }
        }
        return null;
    }

    /**
     * 将单条redis数据插入mysql数据库
     * @param tableId   要插入的mysql表id
     * @param redisData 参考的redis数据信息
     * @param alterInfo 数据的修改信息
     */
    private void redis2MysqlInsert(int tableId,HashMap<String,Object> redisData,HashMap<String,Object> alterInfo){
        //mysqlDataRetention(tableId,this.alterDataByInfo(redisData,alterInfo),OperatorCode.NEW);
    }

    /**
     * 根据单条redis数据更新mysql数据库
     * @param tableId   要插入的mysql表id
     * @param redisData 参考的redis数据信息
     * @param alterInfo 数据的修改信息
     */
    private void redis2MysqlUpdate(int tableId,HashMap<String,Object> redisData,HashMap<String,Object> alterInfo){
        //mysqlDataRetention(tableId,this.alterDataByInfo(redisData,alterInfo),OperatorCode.UPGRADE);
    }

    /**
     * 将数据保存至Mysql数据库
     * @param tableId mysql表id
     * @param data 操作的数据列表，每个hashMap格式如下
     *             {
     *                  "op":OperatorCode   操作码,
     *                  "index": Object       主键,
     *                  "value":{}          新值
     *             }
     * @param record  是否记录当前该组操作
     */
    @SuppressWarnings("unchecked")
    @Transactional
    public void mysqlDataRetention(int tableId,List<HashMap> data,boolean record){
        for(HashMap x:data){
            this.mysqlDataRetention(tableId,JSONHelper.jsonStr2Map(x.get("value").toString()),
                    OperatorCode.valueOf(x.get("op").toString()),record);
        }
    }

    private String getExceptionAllInfo(Exception ex) {
        ByteArrayOutputStream out;
        PrintStream pout = null;
        String ret;
        try {
            out = new ByteArrayOutputStream();
            pout = new PrintStream(out);
            ex.printStackTrace(pout);
            ret = new String(out.toByteArray());
            out.close();
        }
        catch (Exception e) {
            return ex.getMessage();
        }
        finally {
            if (pout != null) {
                pout.close();
            }
        }
        return ret;
    }

    /**
     * 单条数据更新至Mysql数据库
     * @param tableId mysql表id
     * @param data 操作的数据
     * @param opCode 操作类型
     * @param record  是否记录当前操作
     * @return 旧值，无则为null
     */
    @SuppressWarnings("unchecked")
    @Transactional
    public HashMap mysqlDataRetention(int tableId,HashMap data,OperatorCode opCode,boolean record){
        data.put("MODIFY_TIME",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()));
        String tableName = this.tableConfigService.getTableNameById(tableId);
        String primaryKey = this.tableConfigService.getPrimaryKeyByTableId(tableId);
        if(opCode == OperatorCode.NEW){
            if(this.tableConfigService.getColumnType(tableId,primaryKey) == String.class){
                data.put(primaryKey,UUID.randomUUID().toString());
            }
            if(Arrays.asList(this.tableConfigService.getColumnNamesOfTable(tableId)).contains("SEQ_NO")){
                data.put("SEQ_NO",this.greatMapper.getSize(tableName)+1);
            }
            this.greatMapper.insert(tableName,data);
            return null;
        }
        else{
            Object id = data.get(primaryKey);
//            if(this.tableConfigService.getColumnType(tableId,primaryKey) == String.class){
//                id = "'"+id+"'";
//            }
            HashMap oldValue = this.greatMapper.freeInspect(tableName,primaryKey,id.toString()).get(0);
            if(opCode == OperatorCode.UPGRADE){
                this.greatMapper.update(tableName,id,data);
                return oldValue;
            }
            else if(opCode == OperatorCode.DELETE){
                this.greatMapper.delete(tableName,id.toString());
                return oldValue;
            }
            return null;
        }
    }

    /**
     * 根据修改信息对原数据进行修改
     * @param data 修改的对象
     * @param alterInfo 修改信息
     * @return 修改后的对象
     */
    @SuppressWarnings("unchecked")
    private HashMap<String,Object> alterDataByInfo(HashMap data,HashMap<String,Object> alterInfo){
        if(alterInfo == null){
            return data;
        }
        OperatorCode opCode = OperatorCode.valueOf(alterInfo.get("op").toString());
        switch (opCode){
            case NEW:
                return (HashMap<String,Object>)alterInfo.get("newValue");
            case DELETE:
                return null;
            case UPGRADE:
                HashMap<String,Object> newValue = (HashMap<String,Object>)alterInfo.get("newValue");
                for(String key:newValue.keySet()){
                    if(data.containsKey(key)){
                        data.put(key,alterInfo.get(key));
                    }
                }
                return data;
            case SAME:
            default:
                return data;
        }
    }
}
