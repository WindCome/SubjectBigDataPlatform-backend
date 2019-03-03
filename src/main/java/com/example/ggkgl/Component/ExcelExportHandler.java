package com.example.ggkgl.Component;

import com.example.ggkgl.AssitClass.ProcessCallBack;
import com.example.ggkgl.Model.ExportInfo;
import com.example.ggkgl.Service.ThreadManagerService;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
* excel异步数据导出处理器
 **/
@Component
public class ExcelExportHandler implements IExport{

    @Resource
    private ThreadManagerService threadManagerService;

    @Resource
    private NonAsyExcelExportHandler nonAsyExcelExportHandler;

    @Override
    @SuppressWarnings("unchecked")
    public ExportInfo export(List<HashMap> data, JSONObject params, ProcessCallBack callBack){
        Callable<String> callable = () ->
                ExcelExportHandler.this.nonAsyExcelExportHandler.export(data,params,callBack).getValue().toString();
        FutureTask<String> futureTask = new FutureTask<>(callable);
        long jobId = this.threadManagerService.executeThread(futureTask);
        return new ExportInfo(ExportInfo.JOB,jobId);
    }

    @Override
    public String handleType() {
        return "excel";
    }
}