package com.example.ggkgl.Model;

import com.example.ggkgl.Service.DataManagerService;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
*   Mysql单条数据变更记录详情
 **/
@Entity
public class RecordDetailEntity implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Mysql表Id
     */
    private int tableId;

    /**
     * 操作组ID
     */
    private String groupId;

    /**
     * 操作码
     */
    @Enumerated(EnumType.STRING)
    private DataManagerService.OperatorCode op;

    /**
     * 旧值,json格式
     */
    private String oldValue;

    /**
     * 新值,json格式
     */
    private String newValue;

    /**
     * 是否生效
     */
    private boolean valid;

    /**
     * 记录生成时间
     */
    private Timestamp modifyTime;

    public DataManagerService.OperatorCode getOp() {
        return op;
    }

    public void setOp(DataManagerService.OperatorCode op) {
        this.op = op;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public Timestamp getModifyTime() {
        return modifyTime;
    }

    private void setModifyTime(Timestamp modifyTime) {
        this.modifyTime = modifyTime;
    }

    @PrePersist
    private void initModifyTime(){
        this.setModifyTime(new Timestamp(System.currentTimeMillis()));
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public int getTableId() {
        return tableId;
    }

    public Long getId() {
        return id;
    }

    private void setId(Long id) {
        this.id = id;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
