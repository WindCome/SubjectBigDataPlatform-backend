package com.example.ggkgl.Service;

import com.example.ggkgl.Mapper.GreatMapper;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONTokener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
* mysql表配置相关
 **/
@Service
public class TableConfigService {
    @Resource
    private GreatMapper greatMapper;

    /**
     * 获取公共库表的大小（数据条目数）
     * @param tableId 表的ID（即保存在META_ENTITY中的自增字段）
     * @return 返回大小
     */
    public int getSize(int tableId)
    {
        String tableName=this.getTableNameById(tableId);
        return greatMapper.getSize(tableName);
    }

    /**
     * 获取对应公共库的中文名称
     * @param tableId 表的ID（即保存在META_ENTITY中的自增字段）
     * @return 返回对应中文名称
     */
    public String getChineseName(int tableId)
    {
        return greatMapper.freeSearch("META_ENTITY","chinese_name",""+tableId+"");
    }

    /**
    * 根据id获取表的名称
     */
    public String getTableNameById(int tableId){
        return greatMapper.freeSearch("META_ENTITY","name",String.valueOf(tableId));
    }

    /**
     * 获取表的主键
     */
    public String getPrimaryKeyByTableId(int tableId){
        String tableName = this.getTableNameById(tableId);
        return this.greatMapper.findPrimaryKey(tableName);
    }

    /**
    * 获取表的所有字段名
     */
    public String[] getColumnNamesOfTable(int tableId){
        String[] columnNames = this.greatMapper.findColumnName(this.getTableNameById(tableId));
        if(columnNames == null ){
            return new String[0];
        }
        return columnNames;
    }

    /**
     * 获取META_DATA中对应表的matchKeys配置
     */
    public String[] getMatchKeyField(int tableId){
        String tableName = this.getTableNameById(tableId);
        JSONObject allJson=JSONObject.fromObject(greatMapper.getDesc(tableName));
        JSONObject configJson=allJson.getJSONObject("config");
        Object matchKeysConfig = new JSONTokener(configJson.getString("matchKeys")).nextValue();
        List<String> matchKeyList = new ArrayList<>();
        if(matchKeysConfig instanceof JSONArray){
            JSONArray array = (JSONArray)matchKeysConfig;
            for(int i=0;i<array.size();i++){
                matchKeyList.add(array.getString(i));
            }
        }
        else{
            matchKeyList.add(matchKeysConfig.toString());
        }
        return matchKeyList.toArray(new String[0]);
    }

    private static final HashMap<String,Class> mysql2JavaDataType = new HashMap<>();
    static {
        TableConfigService.mysql2JavaDataType.put("varchar",String.class);
        TableConfigService.mysql2JavaDataType.put("text",String.class);
        TableConfigService.mysql2JavaDataType.put("integer",Long.class);
        TableConfigService.mysql2JavaDataType.put("tinyint",Integer.class);
        TableConfigService.mysql2JavaDataType.put("float",Float.class);
        TableConfigService.mysql2JavaDataType.put("boolean",Boolean.class);
        // 其他类型参考https://www.cnblogs.com/jerrylz/p/5814460.html
    }

    /**
    * 查询mysql字段数据类型
     */
    public Class getColumnType(int tableId,String columnName){
        String tableName = this.getTableNameById(tableId);
        String typeName = this.greatMapper.findColumnType(tableName,columnName);
        return TableConfigService.mysql2JavaDataType.get(typeName.toLowerCase());
    }
}
