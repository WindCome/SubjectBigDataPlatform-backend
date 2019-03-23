package com.example.ggkgl.Mapper;

import com.example.ggkgl.Model.LogInfoEntity;
import org.springframework.data.repository.CrudRepository;

public interface LogRepository extends CrudRepository<LogInfoEntity,Integer> {
}
