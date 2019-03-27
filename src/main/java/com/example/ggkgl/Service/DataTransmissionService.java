package com.example.ggkgl.Service;

import com.example.ggkgl.AssitClass.ProcessCallBack;
import com.example.ggkgl.Component.HandlerFactory;
import com.example.ggkgl.Component.Export.IExport;
import com.example.ggkgl.Component.Import.IImport;
import com.example.ggkgl.Model.JobInfo;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
* MYSQL数据导入、导出相关
 **/
@Service
public class DataTransmissionService {
    private final Logger logger = Logger.getLogger(DataTransmissionService.class);

    @Resource
    private MysqlDataManagerService mysqlDataManagerService;

    @Resource
    private HandlerFactory handlerFactory;

    @Resource
    private ThreadManagerService threadManagerService;

    @Resource
    private TableConfigService tableConfigService;

    /**
     * @param target 导出目的类型("excel"、"mysql"等)
     * @param tableId mysql表id
     * @param callBack  导出进度回调
     * @return  导出结果信息
     */
    public JobInfo export(String target, int tableId, JSONObject params, ProcessCallBack callBack) throws Exception {
        List<HashMap> data = mysqlDataManagerService.conditionSearch(null,tableId,-1,-1);
        IExport exportHandler = handlerFactory.getExportHandler(target);
        if(exportHandler == null){
            this.logger.info("找不到合适的数据导出器");
            return new JobInfo();
        }
        return exportHandler.export(data, params,callBack);
    }

    /**
     * @param fromType 导入来源(excel、mysql等)
     * @param params   导入参数 {"tableId":Integer (目的mysql表),
     *                 "start": Boolean (是否立刻启动任务，缺省时代表立即启动),...(其他导出器必要参数)}
     * @param callBack 进度回调
     * @return  导出任务信息
     */
    public JobInfo importData(String fromType,JSONObject params,ProcessCallBack callBack){
        IImport importHandler = handlerFactory.getImportHandler(fromType);
        if(importHandler == null){
            this.logger.info("找不到合适的数据导入器");
            return new JobInfo();
        }
        int tableId = (int)params.get("tableId");
        Thread thread = new Thread(() -> {
            try {
                List<HashMap> dataList = importHandler.importData(params,callBack);
                List<HashMap> opMapList = new ArrayList<>(dataList.size());
                for(HashMap data:dataList){
                    HashMap<String,Object> opMap = new HashMap<>(2);
                    opMap.put("op", MysqlDataManagerService.OperatorCode.NEW);
                    opMap.put("value",data);
                    opMapList.add(opMap);
                }
                mysqlDataManagerService.mysqlDataRetention(tableId,opMapList,callBack,true);
                if(callBack != null){
                    callBack.processFinished(null);
                }
            }
            catch (Exception e) {
                if(callBack != null){
                    callBack.log(e.getMessage());
                    callBack.processFinished(null);
                }
                e.printStackTrace();
            }
        });
        final String startThreadAtOnceParam = "start";
        long jobId;
        if(!params.containsKey(startThreadAtOnceParam) || params.get(startThreadAtOnceParam).equals(true)){
            jobId = this.threadManagerService.executeThread(thread);
        }else {
            jobId = this.threadManagerService.submitThread(thread);
        }
        if(callBack != null){
            callBack.setProgressId(jobId);
        }
        return new JobInfo(JobInfo.JOB,jobId);
    }

    /**
     * 获取用于Excel导入的模板
     */
    public Workbook getExcelTemplateForImport(int tableId){
        HSSFWorkbook workbook = new HSSFWorkbook();
        String sheetName = "sheet1";
        HSSFSheet sheet = workbook.createSheet(sheetName);
        HSSFRow row = sheet.createRow(0);
        HSSFCell cell;
        List<String> title = this.tableConfigService.getModifiableFields(tableId);
        for(int i=0;i<title.size();i++){
            String attribute = title.get(i);
            cell = row.createCell(i);
            cell.setCellValue(attribute);
        }
        return workbook;
    }
}
