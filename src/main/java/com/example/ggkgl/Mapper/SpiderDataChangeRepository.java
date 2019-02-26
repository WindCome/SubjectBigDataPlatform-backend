package com.example.ggkgl.Mapper;

import com.example.ggkgl.Model.SpiderDataChangeEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SpiderDataChangeRepository extends CrudRepository<SpiderDataChangeEntity,Long>{
    SpiderDataChangeEntity findByTableIdEqualsAndIndexEquals(int tableId,int index);

    List<Long> findIdAll();
}
