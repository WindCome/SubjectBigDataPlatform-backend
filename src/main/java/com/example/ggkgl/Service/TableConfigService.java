package com.example.ggkgl.Service;

import com.example.ggkgl.Mapper.GreatMapper;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONTokener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
* mysql表配置相关
 **/
@Service
public class TableConfigService {
    @Resource
    private GreatMapper greatMapper;

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
}
