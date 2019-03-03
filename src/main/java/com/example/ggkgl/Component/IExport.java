package com.example.ggkgl.Component;

import com.example.ggkgl.AssitClass.ProcessCallBack;
import com.example.ggkgl.Model.ExportInfo;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
* 数据导出接口
 **/
public interface IExport {
    /**
     * @param data 要导出的数据
     * @param params    导出参数
     * @param callBack  导出进度回调
     * @return  导出结果信息
     */
    ExportInfo export(List<HashMap> data, JSONObject params, ProcessCallBack callBack) throws Exception;

    /**
     * @return 该导出器导出的目标类型(excel、mysql等)
     */
    String handleType();
}
