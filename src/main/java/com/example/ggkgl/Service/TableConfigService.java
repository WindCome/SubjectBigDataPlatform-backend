package com.example.ggkgl.Service;

import com.example.ggkgl.AssitClass.MysqlHelper;
import com.example.ggkgl.Mapper.GreatMapper;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONTokener;
import org.springframework.cache.annotation.Cacheable;
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
    @Cacheable(value = "tableConfigService", key="#tableId + 'getChineseName'")
    public String getChineseName(int tableId)
    {
        return greatMapper.freeSearch("META_ENTITY","chinese_name",""+tableId+"");
    }

    /**
    * 根据id获取表的名称
     */
    @Cacheable(value = "tableConfigService", key="#tableId + 'getTableNameById'")
    public String getTableNameById(int tableId){
        return greatMapper.freeSearch("META_ENTITY","name",String.valueOf(tableId));
    }

    /**
     * 获取表的主键
     */
    @Cacheable(value = "tableConfigService", key="#tableId + 'getPrimaryKeyByTableId'")
    public String getPrimaryKeyByTableId(int tableId){
        String tableName = this.getTableNameById(tableId);
        return this.greatMapper.findPrimaryKey(tableName);
    }

    /**
    * 获取表的所有字段名
     */
    @Cacheable(value = "tableConfigService", key="#tableId + 'getColumnNamesOfTable'")
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
    @Cacheable(value = "tableConfigService", key="#tableId + 'MatchKeyField'")
    public String[] getMatchKeyField(int tableId){
        String tableName = this.getTableNameById(tableId);
        JSONObject allJson=JSONObject.fromObject(greatMapper.getDesc(tableName));
        JSONObject configJson=allJson.getJSONObject("config");
        if(!configJson.has("matchKeys")){
            return new String[0];
        }
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

    /**
    * 查询mysql字段数据类型
     */
    @Cacheable(value = "tableConfigService", key="#tableId + #columnName + 'ColumnType'")
    public Class getColumnType(int tableId,String columnName){
        String tableName = this.getTableNameById(tableId);
        String typeName = this.greatMapper.findColumnType(tableName,columnName);
        return MysqlHelper.mysqlTypeMapping(typeName.toLowerCase());
    }

    /**
    * 查询某个字段不重复记录的数量
     */
    @Cacheable(value = "tableConfigService", key="#tableName + #columnName + 'ColumnType'")
    public long countDistinctColumn(String tableName,String columnName){
        return this.greatMapper.countDistinctColumn(tableName,columnName);
    }

    /**
     * 查询爬虫启动命令
     */
    public String getSpiderCommand(int tableId){
        String tableName= this.getTableNameById(tableId);
        JSONObject allJson=JSONObject.fromObject(greatMapper.getDesc(tableName));
        JSONObject upgradeJson = allJson.getJSONObject("upgrade");
        if(upgradeJson.has("command")){
            return upgradeJson.getJSONObject("command").getString("value");
        }else{
            return null;
        }
    }

    /**
     * 查询爬虫安装目录
     */
    public String getSpiderPath(int tableId){
        String tableName= this.getTableNameById(tableId);
        JSONObject allJson=JSONObject.fromObject(greatMapper.getDesc(tableName));
        JSONObject upgradeJson = allJson.getJSONObject("upgrade");
        if(upgradeJson.has("path")){
            return upgradeJson.getJSONObject("path").getString("value");
        }else{
            return null;
        }
    }
}
