package com.example.ggkgl.Mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface GreatMapper {
    public void insert(Map<String, Object> params);
    public List<HashMap> desc(@Param("tableName") String tableName,
                              @Param("start") int start,@Param("length") int length);
    public void update(Map<String ,Object> params);
    public void delete(@Param("tableName") String tableName,@Param("Id") String Id);
    public List<HashMap> searchByName(@Param("tableName") String tableName,@Param("name") String name);
}
