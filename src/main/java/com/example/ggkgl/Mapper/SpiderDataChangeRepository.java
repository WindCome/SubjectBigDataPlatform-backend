package com.example.ggkgl.Mapper;

import com.example.ggkgl.Model.SpiderDataChangeEntity;
import org.springframework.data.repository.CrudRepository;

public interface SpiderDataChangeRepository extends CrudRepository<SpiderDataChangeEntity,Long>{
    SpiderDataChangeEntity findByTableIdEqualsAndIndexEquals(int tableId,int index);
}
