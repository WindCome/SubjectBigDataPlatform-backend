package com.example.ggkgl.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

/**
* 日志信息
 **/
@RedisHash("log")
public class LogInfoEntity implements Serializable{
    @Id
    @JsonIgnore
    private String key;

    /**
     * 日志生成时间
     */
    @JsonIgnore
    private long generateTime;

    /**
     * generateTime的视图
     */
    private String generateAtTime;

    /**
     * 某个过程花费的时间ms
     */
    private long spendTime;

    /**
     * 日志详情
     */
    private List<String> detailInfo;

    public LogInfoEntity() {
    }

    public LogInfoEntity(String key) {
        this.key = key;
        this.setGenerateTime(System.currentTimeMillis());
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getGenerateTime() {
        return generateTime;
    }

    private void setGenerateTime(long generateTime) {
        this.generateTime = generateTime;
    }

    public long getSpendTime() {
        return spendTime;
    }

    public void setSpendTime(long spendTime) {
        this.spendTime = spendTime;
    }

    public List<String> getDetailInfo() {
        return detailInfo;
    }

    public void setDetailInfo(List<String> detailInfo) {
        this.detailInfo = detailInfo;
    }

    @Transient
    public String getGenerateAtTime() {
        this.generateAtTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(this.generateTime);
        return this.generateAtTime;
    }
}
