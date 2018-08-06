package com.example.ggkgl.Mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface GreatMapper {
    public void insert(Map<String, Object> params);
    public List<HashMap> desc(@Param("tableName") String tableName,
                              @Param("start") int start,@Param("length") int length,@Param("order")String order );
    public void update(Map<String ,Object> params);
    public void delete(@Param("tableName") String tableName,@Param("Id") String Id);
    public int getSize(@Param("tableName") String tableName);
    public String getDesc(@Param("tableName") String tableName);
    public List<HashMap> freeInspect(@Param("tableName") String tableName,@Param("key") String key
            ,@Param("value")String value);
    public void updateDesc(@Param("tableName")String tableName,@Param("desc")String desc);
    public String freeSearch(@Param("tableName")String tableName,@Param("field") String field
            ,@Param("index") String index);
    public List<HashMap> comboSearch(HashMap map);
}
