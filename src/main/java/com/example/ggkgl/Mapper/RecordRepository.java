package com.example.ggkgl.Mapper;

import com.example.ggkgl.Model.RecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RecordRepository extends JpaRepository<RecordEntity,String>,JpaSpecificationExecutor<RecordEntity> {
}
