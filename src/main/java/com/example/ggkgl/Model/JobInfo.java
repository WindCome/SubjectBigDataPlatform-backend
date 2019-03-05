package com.example.ggkgl.Model;

import java.io.Serializable;

/**
 * 任务信息
 **/
public class JobInfo implements Serializable{
    public static final String REDIS="REDIS";

    public static final String JOB = "JOB";

    public static final String FILE = "FILE";

    private String targetType;

    private Object value;

    public JobInfo() {
    }

    public JobInfo(String targetType, Object value) {
        this.targetType = targetType;
        this.value = value;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
