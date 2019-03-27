package com.example.ggkgl.Mapper;

import javafx.util.Pair;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface GreatMapper {
    void insert(@Param("tableName") String tableName,@Param("data") Map<String, String> params);
    List<HashMap> display(@Param("tableName") String tableName,
                                 @Param("start") int start,@Param("length") int length);
    List<HashMap> desc(@Param("tableName") String tableName,@Param("deleted") boolean deleted,
                              @Param("start") int start,@Param("length") int length,@Param("order")String order );
    void update(@Param("tableName") String tableName,@Param("id")Object id,@Param("data") Map<String, String> params);
    void updateField(@Param("tableName")String tableName,@Param("id")Object id,@Param("field")String field,@Param("value")Object value);
    //void delete(@Param("tableName") String tableName,@Param("Id") String Id);
    int getSize(@Param("tableName") String tableName);
    int getSizeWithCondition(@Param("tableName") String tableName, @Param("deleted") boolean deleted);
    String getDesc(@Param("tableName") String tableName);
    List<HashMap> freeInspect(@Param("tableName") String tableName,@Param("key") String key
            ,@Param("value")String value);
    void updateDesc(@Param("tableName")String tableName,@Param("desc")String desc);
    String freeSearch(@Param("tableName")String tableName,@Param("field") String field
            ,@Param("index") String index);
    List<HashMap> comboSearch(@Param("tableName")String tableName, @Param("conditions")List<Pair<String,String>> conditions, @Param("start") int start, @Param("length") int length);
    long comboCount(@Param("tableName")String tableName,@Param("conditions")List<Pair<String,String>> conditions);
    String findPrimaryKey(@Param("tableName")String tableName);
    String findColumnType(@Param("tableName")String tableName,@Param("columnName") String columnName);
    long countDistinctColumn(@Param("tableName")String tableName,@Param("columnName") String columnName);
    String[] findColumnName(@Param("tableName")String tableName);
}
