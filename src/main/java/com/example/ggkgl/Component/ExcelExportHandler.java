package com.example.ggkgl.Component;

import com.example.ggkgl.AssitClass.ExceptionHelper;
import com.example.ggkgl.AssitClass.ProcessCallBack;
import com.example.ggkgl.Service.ResourceService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.*;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
* excel非阻塞数据导出处理器
 **/
@Component
public class ExcelExportHandler implements IExport{
    private Logger logger = Logger.getLogger(ExceptionHelper.class);
    /**
     * @return {"file": String (文件名)}
     */
    @Override
    @SuppressWarnings("unchecked")
    public HashMap<String, Object> export(List<HashMap> data, JSONObject params, ProcessCallBack callBack) {
        if(CollectionUtils.isEmpty(data)){
            if(callBack != null){
                callBack.log("数据为空");
                callBack.processFinished();
            }
            return new HashMap<>(0);
        }
        String fileName = System.currentTimeMillis()+ RandomStringUtils.randomAlphanumeric(5)+ File.separator+"xlsx";
        Thread thread = new Thread(() -> {
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
        });
        thread.start();
        HashMap<String,Object> result = new HashMap<>(1);
        result.put("file",fileName);
        return result;
    }

    @Override
    public String handleType() {
        return "excel";
    }
}
