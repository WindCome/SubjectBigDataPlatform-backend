package com.example.ggkgl.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import javax.persistence.GeneratedValue;
import java.io.Serializable;
import java.util.HashMap;

/**
* 用于记录修改redis数据的信息
 **/
@RedisHash("spider_change")
public class RedisDataChangeEntity implements Serializable{
    @Id
    @GeneratedValue
    private Long id;

    /**
     * 数据存放在redis中的键值
     */
    @Indexed
    private String redisKey;

    /**
     * 变更的数据在redis数据列表中的下标
     */
    @Indexed
    private Integer index;

    /**
     * 变更的数据作于在Mysql中的数据的主键值
     */
    private Object primaryValue;

    /**
     * 爬虫数据更改后的值
     */
    private HashMap currentValue;

    private Boolean isDelete = false;

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Object getPrimaryValue() {
        return primaryValue;
    }

    public void setPrimaryValue(Object primaryValue) {
        this.primaryValue = primaryValue;
    }

    public HashMap getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(HashMap currentValue) {
        this.currentValue = currentValue;
    }

    public Long getId() {
        return id;
    }

    public boolean isDelete() {
        return isDelete != null && isDelete;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }

    public String getRedisKey() {
        return redisKey;
    }

    public void setRedisKey(String redisKey) {
        this.redisKey = redisKey;
    }
}
