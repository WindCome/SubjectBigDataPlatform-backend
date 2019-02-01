package com.example.ggkgl.Service;

import com.example.ggkgl.Mapper.RecordDetailRepository;
import com.example.ggkgl.Mapper.RecordRepository;
import com.example.ggkgl.Model.RecordDetailEntity;
import com.example.ggkgl.Model.RecordEntity;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;

/**
* Mysql 版本控制相关
 **/
@Service
public class MysqlVersionControlService {
    @Resource private RecordDetailRepository recordDetailRepository;
    @Resource private RecordRepository recordRepository;
    private ThreadLocal<RecordEntity> currentGroupRecord = new ThreadLocal<>();
    private ThreadLocal<Boolean> onlyOneOp = new ThreadLocal<>();
    /**
     * 获取版本变迁的简要说明
     * @return 变迁概要列表 ，数据格式如下
     *          [
     *              {
     *                  "currentVersion":当前版本号，
     *                  "parentVersion":父版本号，多个父版本号时以#分割，
     *                  "updateTime":该组数据的更新时间,
     *                  "newCount":新增数据数量，
     *                  "updateCount":修改数据数量，
     *                  "deleteCount":删除数据的数量
     *              },....{}
     *          ]
     */
    public ArrayList<Object> getVersionHistorySummary(){
        return null;
    }

    /**
     * 获取最新的版本号
     */
    public String getLatestVersion(){
        return null;
    }

    /**
     * 生成一组数据的更改记录
     * @param opCount 该组操作的数量
     */
    public RecordEntity generateRecord(int opCount){
        RecordEntity groupRecord = new RecordEntity();
        String version =this.getLatestVersion();
        groupRecord.setParentVersion(version);
        groupRecord.setOpCount(opCount);
        groupRecord = this.recordRepository.save(groupRecord);
        this.currentGroupRecord.set(groupRecord);
        return groupRecord;
    }


    /**
    * 生成一条数据的更改记录
     */
    public RecordDetailEntity generateRecordDetail(int tableId, HashMap data, DataManagerService.OperatorCode opCode){
        RecordDetailEntity recordDetail = new RecordDetailEntity();
        recordDetail.setTableId(tableId);
        JSONObject jsonObject = JSONObject.fromObject(data);
        recordDetail.setNewValue(jsonObject.toString());
        recordDetail.setOp(opCode);
        RecordEntity groupRecord = this.currentGroupRecord.get();
        if(groupRecord == null){
            groupRecord = this.generateRecord(1);
            this.onlyOneOp.set(true);
        }
        recordDetail.setGroupId(groupRecord.getGroupId());
        return this.recordDetailRepository.save(recordDetail);
    }

    /**
     * 提交一条更改记录
     */
    public void commitRecordDetail(RecordDetailEntity recordDetailEntity){
        recordDetailEntity.setValid(true);
        this.recordDetailRepository.save(recordDetailEntity);
        if(this.onlyOneOp.get()){
            this.commitRecord(this.currentGroupRecord.get());
        }
    }

    /**
     * 提交一组更改记录
     */
    public void commitRecord(RecordEntity recordEntity){
        recordEntity.setValid(true);
        this.currentGroupRecord.remove();
        this.onlyOneOp.remove();
        this.recordRepository.save(recordEntity);
    }
}
