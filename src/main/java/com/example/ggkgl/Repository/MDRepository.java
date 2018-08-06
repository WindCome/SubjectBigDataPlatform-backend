package com.example.ggkgl.Repository;

import com.example.ggkgl.Entity.MetaEntityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MDRepository extends JpaRepository<MetaEntityEntity,Integer> {

    @Query(nativeQuery = true,value = "select * from META_ENTITY limit ?1,?2")
    List<MetaEntityEntity> getPageable(int start,int length);
}
