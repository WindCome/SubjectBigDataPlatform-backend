package com.example.ggkgl.Service;

import com.example.ggkgl.AssitClass.ProcessCallBack;
import com.example.ggkgl.Component.ExportHandlerFactory;
import com.example.ggkgl.Component.IExport;
import com.example.ggkgl.Model.ExportInfo;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
* MYSQL数据导入、导出相关
 **/
@Service
public class DataTransmissionService {
    private final Logger logger = Logger.getLogger(DataTransmissionService.class);

    @Resource
    private DataManagerService dataManagerService;

    @Resource
    private ExportHandlerFactory exportHandlerFactory;

    /**
     * @param target 导出目的类型("excel"、"mysql"等)
     * @param tableId mysql表id
     * @param callBack  导出进度回调
     * @return  导出结果信息
     */
    public ExportInfo export(String target, int tableId, JSONObject params, ProcessCallBack callBack) throws Exception {
        List<HashMap> data = dataManagerService.conditionSearch(null,tableId,-1,-1);
        IExport exportHandler = exportHandlerFactory.getExportHandler(target);
        if(exportHandler == null){
            final String errorInfo = "找不到合适的数据导出器";
            if(callBack != null){
                callBack.log(errorInfo);
                callBack.processFinished();
            }
            this.logger.info(errorInfo);
            return new ExportInfo();
        }
        return exportHandler.export(data, params,callBack);
    }
}
