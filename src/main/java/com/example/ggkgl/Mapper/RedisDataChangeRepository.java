package com.example.ggkgl.Mapper;

import com.example.ggkgl.Model.RedisDataChangeEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RedisDataChangeRepository extends CrudRepository<RedisDataChangeEntity,Long>{
    RedisDataChangeEntity findByRedisKeyEqualsAndIndexEquals(String redisKey, int index);

    @Query(value="select id from RedisDataChangeEntity u")
    List<Long> findId();
}
