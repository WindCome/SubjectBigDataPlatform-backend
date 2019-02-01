package com.example.ggkgl.Model;

import org.apache.commons.lang.RandomStringUtils;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
* Mysql一组数据变更记录摘要
 **/
@Entity
public class RecordEntity implements Serializable{
    /**
     * 操作组ID
     */
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String groupId;

    /**
     * 父版本号，存在多个父版本时以#分割
     */
    private String parentVersion;

    /**
     * 数据更新后的版本号
     */
    @Column(unique = true)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String currentVersion;

    /**
     * 该组包含的操作数
     */
    private int opCount;

    /**
     * 是否生效
     */
    private boolean valid;

    /**
     * 记录生成时间
     */
    private Timestamp modifyTime;

    public String getGroupId() {
        return groupId;
    }

    private void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getParentVersion() {
        return parentVersion;
    }

    public String[] getParentVersionArray(){
        if(StringUtils.isEmpty(this.parentVersion)){
            return new String[0];
        }
        return this.parentVersion.split("#");
    }

    public void setParentVersion(String parentVersion) {
        this.parentVersion = parentVersion;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    private void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    public Timestamp getModifyTime() {
        return modifyTime;
    }

    private void setModifyTime(Timestamp modifyTime) {
        this.modifyTime = modifyTime;
    }

    @PrePersist
    private void initFields(){
        this.setModifyTime(new Timestamp(System.currentTimeMillis()));
        this.setCurrentVersion(System.currentTimeMillis()+ RandomStringUtils.randomAlphanumeric(5));
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public int getOpCount() {
        return opCount;
    }

    public void setOpCount(int opCount) {
        this.opCount = opCount;
    }
}
