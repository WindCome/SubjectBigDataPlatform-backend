package com.example.ggkgl.Component;

import com.example.ggkgl.AssitClass.ProcessCallBack;
import com.example.ggkgl.Model.ExportInfo;
import com.example.ggkgl.Service.ResourceService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
* 非异步Excel导出器
 **/
@Component
public class NonAsyExcelExportHandler implements IExport{
    private Logger logger = Logger.getLogger(NonAsyExcelExportHandler.class);

    @Override
    @SuppressWarnings("unchecked")
    public ExportInfo export(List<HashMap> data, JSONObject params, ProcessCallBack callBack) throws Exception {
        if(CollectionUtils.isEmpty(data)){
            throw new Exception("excel导出数据为空");
        }
        String fileName = params.containsKey("file")?
                System.currentTimeMillis()+ RandomStringUtils.randomAlphanumeric(5)+ File.separator+"xlsx" :
                params.get("file").toString();
        int totalRow = data.size()+1;
        HSSFWorkbook workbook = new HSSFWorkbook();
        String sheetName = "sheet1";
        if(params.containsKey("sheetName")){
            sheetName = params.getString("sheetName");
        }
        HSSFSheet sheet = workbook.createSheet(sheetName);
        HSSFRow row = sheet.createRow(0);
        HSSFCell cell;
        if(callBack != null){
            callBack.log("正在导入excel");
        }
        //表头
        List<String> title = new ArrayList<>(data.get(0).keySet());
        HashMap<String,Integer> attributeToIndex = new HashMap<>(title.size());
        for(int i=0;i<title.size();i++){
            String attribute = title.get(i);
            cell = row.createCell(i);
            cell.setCellValue(attribute);
            attributeToIndex.put(attribute,i);
        }
        if(callBack != null){
            float process=1.0f/totalRow;
            int percent=(int)Math.rint(process*100);
            callBack.onProcessChange(percent);
        }
        //内容
        for(int i=0;i<data.size();i++){
            row = sheet.createRow(i + 1);
            HashMap rowData = data.get(i);
            for(Object key:rowData.keySet()){
                row.createCell(attributeToIndex.get(key.toString())).setCellValue(rowData.get(key).toString());
            }
            if(callBack != null){
                float process=1.0f/totalRow;
                int percent=(int)Math.rint(process*100);
                callBack.onProcessChange(percent);
            }
        }
        if(callBack != null){
            callBack.log("正在保存excel");
        }
        String storagePath = ResourceService.STORAGE_FILE_PATH + File.separator +fileName;
        File file = new File(storagePath);
        try(FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
            if(callBack != null){
                callBack.processFinished();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            this.logger.info(e.getMessage());
            if(callBack != null){
                callBack.log("保存excel时发生错误:"+e.getMessage());
                callBack.processFinished();
            }
        }
        return new ExportInfo(ExportInfo.FILE,storagePath);
    }

    @Override
    public String handleType() {
        return "nonAsyExcel";
    }
}
