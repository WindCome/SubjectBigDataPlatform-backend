package com.example.ggkgl.Service;

import com.example.ggkgl.AssitClass.JSONHelper;
import com.example.ggkgl.AssitClass.ProcessCallBack;
import com.example.ggkgl.Component.SpringUtil;
import com.example.ggkgl.Mapper.GreatMapper;
import javafx.util.Pair;
import net.sf.json.JSONObject;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
*  数据增删查改相关
**/
@Service
public class MysqlDataManagerService {
    public enum OperatorCode{
        NEW("NEW"),UPDATE("UPDATE"),DELETE("DELETE");
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
    private TableConfigService tableConfigService;

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
                    if(targetValue == null){
                        continue;
                    }
                    boolean o1Match = targetValue.equals(o1.get(key));
                    boolean o2Match = targetValue.equals(o2.get(key));
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
            resultMap.put("data",new ArrayList<>(0));
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
        int sizeOfTable = this.tableConfigService.getSize(tableId);
        if(sizeOfTable == 0 || columnNames.length == 0){
            return columnNames;
        }
        LinkedHashMap<String,Float> columnToDiversityFactorMap = new LinkedHashMap<>(columnNames.length);
        for(String c:columnNames){
            float factor = (float)this.tableConfigService.countDistinctColumn(tableName,c)/sizeOfTable;
            columnToDiversityFactorMap.put(c,factor);
        }
        columnToDiversityFactorMap = this.sortMapByValue(columnToDiversityFactorMap);
        return columnToDiversityFactorMap.keySet().toArray(new String[0]);
    }

    /**
     * Map按value进行排序
     */
    private LinkedHashMap<String, Float> sortMapByValue(Map<String, Float> oriMap) {
        if (oriMap == null || oriMap.isEmpty()) {
            return new LinkedHashMap<>(0);
        }
        LinkedHashMap<String, Float> sortedMap = new LinkedHashMap<>();
        List<Map.Entry<String, Float>> entryList = new ArrayList<>(oriMap.entrySet());
        entryList.sort((o1, o2) -> {
            float val1 = o1.getValue();
            float val2 = o2.getValue();
            if(val1<val2){
                return 1;
            }else if (val1 > val2){
                return -1;
            }
            return 0;
        });

        for (Map.Entry<String, Float> tmpEntry : entryList) {
            sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
        }
        return sortedMap;
    }

    /**
     * 将数据保存至Mysql数据库
     * @param tableId mysql表id
     * @param data 操作的数据列表，每个hashMap格式如下
     *             {
     *                  "op":OperatorCode   操作码,
     *                  "index": Object       主键,
     *                  "value":{}或String          新值
     *             }
     * @param record  是否记录当前该组操作
     */
    @SuppressWarnings("unchecked")
    @Transactional
    public void mysqlDataRetention(int tableId, List<HashMap> data, ProcessCallBack processCallBack, boolean record){
        if(data == null || data.size() == 0){
            return ;
        }
        int totalCount = data.size();
        for(int i = 0 ; i < totalCount ; i++){
            HashMap x = data.get(i);
            Object valueField = x.get("value");
            if(valueField !=null && valueField instanceof String){
                valueField = JSONHelper.jsonStr2Map(valueField.toString());
            }
            //直接用this调用会导致aop失效
            SpringUtil.getBean(this.getClass()).mysqlDataRetention(tableId,x.get("index"),(HashMap) valueField,
                    OperatorCode.valueOf(x.get("op").toString()),record);
            if(processCallBack != null){
                float process=(float)i/totalCount;
                int percent=(int)Math.rint(process*100);
                processCallBack.onProcessChange(percent);
            }
        }
    }

    /**
     * 单条数据更新至Mysql数据库
     * @param tableId mysql表id
     * @param data 操作的数据
     * @param opCode 操作类型
     * @param record  是否记录当前操作,(aop调用,不要删除这个参数)
     * @return 旧值，无则为null
     */
    @SuppressWarnings("unchecked")
    @Transactional
    @CacheEvict(value = "RedisContrastResult",allEntries = true)
    public HashMap mysqlDataRetention(int tableId,Object id, HashMap data,OperatorCode opCode,boolean record){
        String primaryKey = this.tableConfigService.getPrimaryKeyByTableId(tableId);
        if(data!=null){
            data.put("MODIFY_TIME",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()));
            data.remove(primaryKey);
        }
        String tableName = this.tableConfigService.getTableNameById(tableId);
        if(opCode == OperatorCode.NEW){
            Assert.notNull(data,"用于更新的数据不能为空");
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
            HashMap oldValue = this.greatMapper.freeInspect(tableName,primaryKey,id.toString()).get(0);
            if(this.tableConfigService.getColumnType(tableId,primaryKey) == String.class){
                id = "'"+id+"'";
            }
            final String deleteFieldName = TableConfigService.deleteFieldName;
            if(opCode == OperatorCode.UPDATE){
                //标记是否删除字段直接使用true或false更新会报错，要先转换为int
                if (data != null && data.containsKey(deleteFieldName)) {
                    boolean deleted = (boolean) data.get(deleteFieldName);
                    data.put(deleteFieldName, deleted ? 1 : 0);
                }
                this.greatMapper.update(tableName,id,data);
                return oldValue;
            }
            else if(opCode == OperatorCode.DELETE){
                this.greatMapper.updateField(tableName,id,deleteFieldName,true);
                return oldValue;
            }
            return null;
        }
    }

    /**
     * 根据主键查找
     * @param tableId 表id
     * @param id 要查找对象的主键的值
     */
    public HashMap findByIdEquals(int tableId,@NotNull Object id){
        String primaryKey = this.tableConfigService.getPrimaryKeyByTableId(tableId);
        String tableName = this.tableConfigService.getTableNameById(tableId);
        List<HashMap> result = this.greatMapper.freeInspect(tableName,primaryKey,id.toString());
        return result == null ? null : result.get(0);
    }

    /**
     * 条件查询
     * @param conditionFilter 筛选条件{筛选字段:目的值}
     * @param tableId  表id
     * @param page 页号(从1开始，<=0代表不分页)
     * @param size 页大小(<=0代表不分页)
     * @return 查询结果
     */
    public List<HashMap> conditionSearch(JSONObject conditionFilter,int tableId,int page,int size){
        String tableName=this.tableConfigService.getTableNameById(tableId);
        int start=(page-1)*size;
        if(conditionFilter == null){
            conditionFilter = new JSONObject();
        }
        return greatMapper.comboSearch(tableName, this.filter2List(conditionFilter),start,size);
    }

    /**
     * 条件统计
     * @param conditionFilter 筛选条件{筛选字段:目的值}
     * @param tableId  表id
     * @return 统计结果
     */
    public long conditionCount(JSONObject conditionFilter,int tableId){
        String tableName=this.tableConfigService.getTableNameById(tableId);
        return greatMapper.comboCount(tableName,this.filter2List(conditionFilter));
    }

    private List<Pair<String,String>> filter2List(JSONObject conditionFilter){
        List<Pair<String,String>> conditions=new ArrayList<>();
        for(Object key:conditionFilter.keySet())
        {
            Pair<String,String> condition = new Pair<>(key.toString(),conditionFilter.get(key).toString());
            conditions.add(condition);
        }
        return conditions;
    }
}
