package com.example.ggkgl.Component.Import;

import com.example.ggkgl.AssitClass.MysqlHelper;
import com.example.ggkgl.AssitClass.ProcessCallBack;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
* Mysql导入器
 **/
@Component
public class MysqlImportHandler implements IImport{
    @Override
    @SuppressWarnings("unchecked")
    public List<HashMap> importData(JSONObject params, ProcessCallBack callBack) throws Exception {
        if(!params.containsKey("host") || !params.containsKey("user")
                || !params.containsKey("password") || !params.containsKey("schema")){
            throw new Exception("缺少连接参数");
        }else if(!params.containsKey("tableName")){
            throw new Exception("缺少查询参数:tableName");
        }
        Connection connection = MysqlHelper.connectToRemoteMysqlServer(params.getString("host"),
                (int)params.getOrDefault("port",3306),params.getString("schema"),
                params.getString("user"),params.getString("password"));
        List<HashMap> dataList = MysqlHelper.selectAll(connection,params.getString("tableName"));
        if(params.containsKey("map")){
            if(callBack != null){
                callBack.log("正在映射");
            }
            JSONObject keyConfig = params.getJSONObject("map");
            List<HashMap> result = new ArrayList<>(dataList.size());
            for(int i = 0;i<dataList.size();i++){
                HashMap data = dataList.get(i);
                HashMap newData = new HashMap(data.size());
                for(Object key:keyConfig.keySet()){
                    if(!data.containsKey(key)){
                        throw new Exception("映射缺失键:"+key.toString());
                    }
                    newData.put(keyConfig.getString(key.toString()),data.get(key));
                }
                result.add(newData);
                if(callBack != null){
                    float process=(float)(i)/dataList.size();
                    int percent=(int)Math.rint(process*100);
                    callBack.onProcessChange(percent);
                }
            }
            return result;
        }
        return dataList;
    }

    @Override
    public String handleType() {
        return "mysql";
    }
}
