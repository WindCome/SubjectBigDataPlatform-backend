package com.example.ggkgl.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import javax.persistence.GeneratedValue;
import java.io.Serializable;
import java.util.Map;

/**
* 用于记录修改爬虫数据的信息
 **/
@RedisHash("spider_change")
public class SpiderDataChangeEntity implements Serializable{
    @Id
    @GeneratedValue
    private Long id;
    /**
     * 变更的数据在爬虫数据列表中的下标
     */
    private Integer index;

    /**
     * mysql表id
     */
    private int tableId;

    /**
     * 变更的数据作于在Mysql中的数据的主键值
     */
    private Object primaryValue;

    /**
     * 爬虫数据更改后的值
     */
    private Map currentValue;

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public Object getPrimaryValue() {
        return primaryValue;
    }

    public void setPrimaryValue(Object primaryValue) {
        this.primaryValue = primaryValue;
    }

    public Map getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(Map currentValue) {
        this.currentValue = currentValue;
    }
}
