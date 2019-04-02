package com.example.ggkgl.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

/**
* 日志信息
 **/
@RedisHash("log")
public class LogInfoEntity implements Serializable{
    @Id
    @JsonIgnore
    private int key;

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

    /**
     * 上一个版本的统计情况
     */
    private HashMap<String,Object> lastURLCount = new HashMap<>(0);

    /**
     * 当前爬取链接的统计情况
     */
    private HashMap<String,Object> currentURLCount = new HashMap<>(0);

    public LogInfoEntity() {
    }

    public LogInfoEntity(int key) {
        this.key = key;
        this.setGenerateTime(System.currentTimeMillis());
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
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

    public HashMap<String, Object> getLastURLCount() {
        return lastURLCount;
    }

    public void setLastURLCount(HashMap<String, Object> lastURLCount) {
        this.lastURLCount = lastURLCount;
    }

    public HashMap<String, Object> getCurrentURLCount() {
        return currentURLCount;
    }

    public void setCurrentURLCount(HashMap<String, Object> currentURLCount) {
        this.currentURLCount = currentURLCount;
    }
}
