package com.example.ggkgl.Mapper;

import com.example.ggkgl.AssitClass.RecordDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RecordDetailRepository extends JpaRepository<RecordDetailEntity,Long>,JpaSpecificationExecutor<RecordDetailEntity> {
}
