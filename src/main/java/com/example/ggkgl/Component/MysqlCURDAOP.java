package com.example.ggkgl.Component;

import com.example.ggkgl.AssitClass.JSONHelper;
import com.example.ggkgl.Model.RecordDetailEntity;
import com.example.ggkgl.Model.RecordEntity;
import com.example.ggkgl.Service.DataManagerService;
import com.example.ggkgl.Service.MysqlVersionControlService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
* Mysql CURD AOP
 **/
@Aspect
@Configuration
public class MysqlCURDAOP {

    @Resource
    private MysqlVersionControlService mysqlVersionControlService;

    /**
    * 对单条数据操作的AOP环绕
     */
    @Around(value = "execution(* mysqlDataRetention(..)) &&"+"args(tableId,id,data,opCode,record)",
            argNames = "thisJoinPoint,tableId,id,data,opCode,record")
    public Object aopPerRecord(ProceedingJoinPoint thisJoinPoint,int tableId,Object id,HashMap data,
                             DataManagerService.OperatorCode opCode,boolean record) throws Throwable {
        Object returnValue;
        RecordDetailEntity recordDetail = null;
        //生成记录
        if(record){
            recordDetail = this.mysqlVersionControlService.generateRecordDetail(tableId,id,data,opCode);
        }
        //执行
        returnValue =  thisJoinPoint.proceed();
        //记录生效
        if(record){
            recordDetail.setOldValue(JSONHelper.map2Json((HashMap)returnValue));
            this.mysqlVersionControlService.commitRecordDetail(recordDetail);
        }
        return returnValue;
    }

    /**
     * 对一组数据操作的AOP环绕
     */
    @Around(value = "execution(* mysqlDataRetention(..)) &&"+"args(tableId,data,record)",
            argNames = "thisJoinPoint,tableId,data,record")
    public Object aopPerGroupRecord(ProceedingJoinPoint thisJoinPoint, int tableId, List<HashMap> data, boolean record) throws Throwable {
        Object returnValue;
        RecordEntity recordEntity = null;
        //生成记录
        if(record){
            recordEntity = this.mysqlVersionControlService.generateRecord(data.size());
        }
        //执行
        returnValue =  thisJoinPoint.proceed();
        //记录生效
        if(record){
            this.mysqlVersionControlService.commitRecord(recordEntity);
        }
        return returnValue;
    }
}
