package com.example.ggkgl.Service;

import com.example.ggkgl.Mapper.RecordDetailRepository;
import com.example.ggkgl.Mapper.RecordRepository;
import com.example.ggkgl.Model.RecordDetailEntity;
import com.example.ggkgl.Model.RecordEntity;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

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
     * 获取版本变迁列表
     * @return 变迁概要列表 ，数据格式如下
     *          [
     *              {
     *                  "parentIndex":[]父版本下标，
     *                  "childIndex":[]子版本下标
     *                  "record":RecordEntity 记录信息
     *              },....{}
     *          ]
     */
    public List<HashMap<String,Object>> getVersionHistory(){
        final String recordStr = "record";
        final String parentIndexStr = "parentIndex";
        final String childIndexStr = "childIndex";
        List<HashMap<String,Object>> result = new ArrayList<>();
        List<RecordEntity> recordEntities = this.recordRepository.findAll();
        HashMap<String,Integer> version2Index = new HashMap<>();
        for(RecordEntity x:recordEntities){
            HashMap<String,Object> tmp =new HashMap<>(3);
            tmp.put(recordStr,x);
            version2Index.put(x.getCurrentVersion(),result.size());
            result.add(tmp);
        }
        for(int i=0;i<result.size();i++){
            HashMap<String,Object> x = result.get(i);
            RecordEntity recordEntity = (RecordEntity)x.get(recordStr);
            String[] parentVersions = recordEntity.getParentVersionArray();
            List<Integer> parentIndexList = new ArrayList<>(parentVersions.length);
            for(String parentVersion:parentVersions){
                int parentIndex = version2Index.get(parentVersion);
                parentIndexList.add(parentIndex);
                HashMap<String,Object> parentRecordHashMap = result.get(parentIndex);
                if(!parentRecordHashMap.containsKey(childIndexStr)){
                    parentRecordHashMap.put(childIndexStr,new ArrayList<Integer>());
                }
                @SuppressWarnings("unchecked")
                List<Integer> childIndexList = (List<Integer>) parentRecordHashMap.get(childIndexStr);
                childIndexList.add(i);
            }
            x.put(parentIndexStr,parentIndexList);
        }
        return result;
    }

    /**
     * 获取最新的版本号
     */
    public List<String> getLatestVersions(){
        List<HashMap<String,Object>> history = this.getVersionHistory();
        List<Integer> rootIndex = new ArrayList<>();
        for(int i = 0;i<history.size();i++){
            HashMap<String,Object> x = history.get(i);
            @SuppressWarnings("unchecked")
            List<Integer> parentIndexList = (List<Integer>)x.get("parentIndex");
            if(parentIndexList == null || parentIndexList.size() == 0){
                rootIndex.add(i);
            }
        }
        Queue<Integer> queue = new ArrayDeque<>(rootIndex);
        List<Integer> resultIndex = new ArrayList<>();
        while (!queue.isEmpty()){
            int index = queue.poll();
            @SuppressWarnings("unchecked")
            List<Integer> childIndex = (List<Integer>)history.get(index).get("childIndex");
            if((childIndex == null ||childIndex.size() == 0) && !resultIndex.contains(index)){
                resultIndex.add(index);
            }
            else if(childIndex != null) {
                queue.addAll(childIndex);
            }
        }
        List<String> result = new ArrayList<>(resultIndex.size());
        for(int x :resultIndex){
            RecordEntity record = (RecordEntity)history.get(x).get("record");
            result.add(record.getCurrentVersion());
        }
        return result;
    }

    /**
     * 生成一组数据的更改记录
     * @param opCount 该组操作的数量
     */
    public RecordEntity generateRecord(int opCount) throws Exception {
        RecordEntity groupRecord = new RecordEntity();
        List<String> currentVersions = this.getLatestVersions();
        if(currentVersions.size()>1){
            throw new Exception("需要合并");
        }
        String version = currentVersions.size() == 1?currentVersions.get(0):null;
        groupRecord.setParentVersion(version);
        groupRecord.setOpCount(opCount);
        groupRecord = this.recordRepository.save(groupRecord);
        this.currentGroupRecord.set(groupRecord);
        return groupRecord;
    }


    /**
    * 生成一条数据的更改记录
     */
    public RecordDetailEntity generateRecordDetail(int tableId,Object id, HashMap data, DataManagerService.OperatorCode opCode) throws Exception {
        RecordDetailEntity recordDetail = new RecordDetailEntity();
        recordDetail.setTableId(tableId);
        if(data!=null){
            JSONObject jsonObject = JSONObject.fromObject(data);
            recordDetail.setNewValue(jsonObject.toString());
        }
        if(id != null){
            recordDetail.setObjectId(id.toString());
        }
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
        Boolean onlyOne = this.onlyOneOp.get();
        if(onlyOne!=null&&onlyOne){
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
