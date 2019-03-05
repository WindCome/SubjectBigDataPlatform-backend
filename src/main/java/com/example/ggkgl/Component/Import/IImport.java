package com.example.ggkgl.Component.Import;

import com.example.ggkgl.AssitClass.ProcessCallBack;
import com.example.ggkgl.Model.JobInfo;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
* 导入接口
 **/
public interface IImport {
    /**
     * @param params    导入参数
     * @param callBack  进度回调
     * @return 该导入任务信息
     */
    List<HashMap> importData(JSONObject params, ProcessCallBack callBack) throws Exception;

    /**
     * @return 该导入器支持的导入来源(excel、redis等)
     */
    String handleType();
}
