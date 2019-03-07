package com.example.ggkgl.Component.Import;

import com.example.ggkgl.AssitClass.ProcessCallBack;
import com.example.ggkgl.Service.ResourceService;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
* Excel导入器
 **/
@Component
public class ExcelImportHandler implements IImport{

    private Logger logger = Logger.getLogger(ExcelImportHandler.class);

    /**
     * @param params   导出参数 {"file":String(指定导入的文件)}
     * @param callBack 导出进度回调
     * @return 导出信息
     */
    @Override
    public List<HashMap> importData(@NotNull JSONObject params, ProcessCallBack callBack) throws Exception {
        if(!params.containsKey("file")){
            final String errorInfo = "缺失必要参数:file";
            this.logger.info(errorInfo);
            throw new Exception(errorInfo);
        }
        final String filePath = ResourceService.STORAGE_FILE_PATH + File.separator +params.get("file").toString();
        try(FileInputStream fileInputStream = new FileInputStream(filePath)){
            List<HashMap> result = new ArrayList<>();
            Workbook workbook = WorkbookFactory.create(fileInputStream);
            final int sheetNumber = workbook.getNumberOfSheets();
            int totalNumber = sheetNumber;
            int finishNumber = 0;
            if(callBack != null){
                //计算总数
                for(int i=0;i<sheetNumber;i++){
                    Sheet sheet = workbook.getSheetAt(i);
                    totalNumber += sheet.getLastRowNum()-sheet.getFirstRowNum();
                }
            }
            for(int i=0;i<sheetNumber;i++){
                Sheet sheet = workbook.getSheetAt(i);
                //默认第一行为表头
                int firstRowIndex = sheet.getFirstRowNum();
                int lastRowIndex = sheet.getLastRowNum();
                HashMap<Integer,String> headerMap = this.getIndex2AttributeMap(sheet.getRow(firstRowIndex));
                for(int j=firstRowIndex+1;j<=lastRowIndex;j++){
                    Row dataRow = sheet.getRow(j);
                    int cellNumber = dataRow.getLastCellNum();
                    HashMap<String,String> data = new HashMap<>(cellNumber);
                    for(int k=0;k< cellNumber;k++){
                        data.put(headerMap.get(k),dataRow.getCell(k).getStringCellValue());
                    }
                    result.add(data);
                    finishNumber++;
                    if(callBack != null){
                        float process=(float)finishNumber/totalNumber;
                        int percent=(int)Math.rint(process);
                        callBack.onProcessChange(percent);
                    }
                }
            }
            return result;
        }
        catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    private HashMap<Integer,String> getIndex2AttributeMap(Row headerRow){
        int cellNumber = headerRow.getLastCellNum();
        HashMap<Integer,String> result = new HashMap<>(cellNumber);
        for(int i=0;i<cellNumber;i++){
            result.put(i,headerRow.getCell(i).getStringCellValue());
        }
        return result;
    }

    @Override
    public String handleType() {
        return "excel";
    }
}
