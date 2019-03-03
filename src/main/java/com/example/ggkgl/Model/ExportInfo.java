package com.example.ggkgl.Model;

import java.io.Serializable;

/**
 * 导出信息
 **/
public class ExportInfo implements Serializable{
    public static final String REDIS="REDIS";

    public static final String JOB = "JOB";

    public static final String FILE = "FILE";

    private String targetType;

    private Object value;

    public ExportInfo() {
    }

    public ExportInfo(String targetType, Object value) {
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
