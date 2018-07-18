package com.example.ggkgl.Repository;

import com.example.ggkgl.Entity.AcademicianEntity;
import com.example.ggkgl.Tool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface AcRepository extends JpaRepository<AcademicianEntity,Integer> {
    public List<AcademicianEntity> findAcademicianEntitiesByNameContaining(String name);

    @Transactional
    @Modifying
    @Query(nativeQuery = true,value ="insert into ?1 values(?2)")
        void Insert(String tableName,List<String> fields);
    }
