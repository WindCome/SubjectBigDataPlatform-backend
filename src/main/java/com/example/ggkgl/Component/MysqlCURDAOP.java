package com.example.ggkgl.Component;

import com.example.ggkgl.AssitClass.RecordDetailEntity;
import com.example.ggkgl.AssitClass.RecordEntity;
import com.example.ggkgl.Mapper.RecordDetailRepository;
import com.example.ggkgl.Mapper.RecordRepository;
import com.example.ggkgl.Service.DataManagerService;
import net.sf.json.JSONObject;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.HashMap;

/**
* Mysql CURD AOP
 **/
@Aspect
@Configuration
public class MysqlCURDAOP {
    private ThreadLocal<String> curdGroupCode = new ThreadLocal<>();

    @Resource
    private RecordRepository recordRepository;

    @Resource
    private RecordDetailRepository recordDetailRepository;

    /**
    * 对单条数据操作的AOP环绕
     */
    @Around(value = "execution(* mysqlDataRetention(..)) &&"+"args(tableId,data,opCode)",
            argNames = "thisJoinPoint,tableId,data,opCode")
    public Object aopPerRecord(ProceedingJoinPoint thisJoinPoint,int tableId,HashMap data,
                             DataManagerService.OperatorCode opCode){
        System.out.println ("====AOP=====");
        Object returnValue = null;
        try {
            //生成记录
            RecordDetailEntity recordDetail = this.generateRecordDetail(tableId,data,opCode);
            returnValue =  thisJoinPoint.proceed();
            //执行
            recordDetail.setValid(true);
            //记录生效
            this.recordDetailRepository.save(recordDetail);
            return returnValue;
        } catch (Throwable e) {
            e.printStackTrace ();
        }
        return null;
    }

    private RecordDetailEntity generateRecordDetail(int tableId,HashMap data,DataManagerService.OperatorCode opCode){
        RecordDetailEntity recordDetail = new RecordDetailEntity();
        recordDetail.setTableId(tableId);
        JSONObject jsonObject = JSONObject.fromObject(data);
        recordDetail.setNewValue(jsonObject.toString());
        recordDetail.setOp(opCode);
        recordDetail.setGroupId(this.curdGroupCode.get());
        //TODO
        //recordDetail.setOldValue();
        return this.recordDetailRepository.save(recordDetail);
    }

    /**
     * 对一组数据操作的AOP环绕
     */
    @Around(value = "execution(* saveData(..)) &&"+"args(version,..)")
    public Object aopPerGroup(ProceedingJoinPoint thisJoinPoint,String version){
        System.out.println ("====GroupAOP=====");
        Object returnValue = null;
        try {
            //生成记录
            RecordEntity record = new RecordEntity();
            record.setParentVersion(version);
            record = this.recordRepository.save(record);
            this.curdGroupCode.set(record.getGroupId());
            //执行
            returnValue =  thisJoinPoint.proceed();
            //记录生效
            record.setValid(true);
            this.recordRepository.save(record);
            return returnValue;
        } catch (Throwable e) {
            e.printStackTrace ();
        }finally {
            this.curdGroupCode.remove();
        }
        return null;
    }
}
