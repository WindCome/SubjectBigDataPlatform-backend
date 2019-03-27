package com.example.ggkgl.Controller;

import com.example.ggkgl.AssitClass.*;
import com.example.ggkgl.Mapper.GreatMapper;
import com.example.ggkgl.Model.JobInfo;
import com.example.ggkgl.Service.*;
import net.sf.json.JSONObject;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.naming.OperationNotSupportedException;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.*;


/**
 * 包含所有的增删查改等基本接口
 * 所有api文档地址：
 * https://docs.qq.com/doc/BqI21X2yZIht15KNMU0dC08g0ev0tg1N54I42NrF4K1ONwO73IQmKC2Cjyb92O8xq00FpNte0vqpPK1D7MU24?opendocxfrom=admin
 */
@RestController
@CrossOrigin
public class AllController {
    @Resource
    private GreatMapper greatMapper;

    @Resource
    private MysqlDataManagerService mysqlDataManagerService;

    @Resource
    private TableConfigService tableConfigService;

    @Resource
    private DataTransmissionService dataTransmissionService;

    @Resource
    private JmsMessagingTemplate jmsMessagingTemplate;

    @Resource
    private ThreadManagerService threadManagerService;

    @Resource
    private ResourceService resourceService;

    /**
     * 获取所有公共库的列表
     */
    @GetMapping(value = "/getAll")
    public List<HashMap> getAll(@RequestParam("page")int page,@RequestParam("size") int size)
    {
        int start=(page-1)*size;
        return greatMapper.display("META_ENTITY",start,size);
    }

    /**
     * 统一增加接口
     * @param tableId  mysql表id
     * @param jsonObject 增加记录需要的json对象，具体格式参照api文档
     * @return  成功时为null，失败时为出错信息
     */
    @PostMapping(value = "/add/{tableId}")
    public String add(@PathVariable("tableId") int tableId, @RequestBody JSONObject jsonObject) {
        try{
            HashMap<String,Object> data = new HashMap<>();
            data.put("op", MysqlDataManagerService.OperatorCode.NEW);
            data.put("value",jsonObject.toString());
            this.mysqlDataManagerService.mysqlDataRetention(tableId, Collections.singletonList(data),null,true);
            return null;
        }catch (Exception e){
            e.printStackTrace();
            return ExceptionHelper.getExceptionAllInfo(e);
        }
    }

    /**
     * 统一删除接口
     * @param id 删除数据的Id
     * @param tableId 表的Id（即保存在META_ENTITY中的自增字段）
     * @return 同添加接口
     */
    @GetMapping(value = "/delete/{tableId}")
    public String delete(@RequestParam("Id") String id,@PathVariable("tableId") int tableId)
    {
        try{
            HashMap<String,Object> data = new HashMap<>();
            data.put("op", MysqlDataManagerService.OperatorCode.DELETE);
            data.put("index",id);
            this.mysqlDataManagerService.mysqlDataRetention(tableId, Collections.singletonList(data),null,true);
            return null;
        }catch (Exception e){
            e.printStackTrace();
            return ExceptionHelper.getExceptionAllInfo(e);
        }
    }

    /**
     * 统一数据修改接口
     * @param tableId 表的ID（即保存在META_ENTITY中的自增字段）
     * @param id 修改数据的Id（即数据库中主键）
     * @param jsonObject 修改json对象（键值对格式），具体参照api文档
     * @return 同添加接口
     */
    @PostMapping(value = "/update/{tableId}")
    public String update(@PathVariable("tableId") int tableId,@RequestParam(value = "Id") String id,
                                      @RequestBody JSONObject jsonObject)
    {
        try{
            HashMap<String,Object> data = new HashMap<>();
            data.put("op", MysqlDataManagerService.OperatorCode.UPDATE);
            data.put("index",id);
            data.put("value",jsonObject.toString());
            this.mysqlDataManagerService.mysqlDataRetention(tableId, Collections.singletonList(data),null,true);
            return null;
        }catch (Exception e){
            e.printStackTrace();
            return ExceptionHelper.getExceptionAllInfo(e);
        }
    }

    /**
     *获取配置接口
     * @param tableId 表的ID（即保存在META_ENTITY中的自增字段）
     * @return  返回某个公共库的各种配置信息，具体见API文档
     */
    @GetMapping(value = "/getConfig/{tableId}")
    public HashMap displayConfig(@PathVariable("tableId") int tableId)
    {
        String tableName=this.tableConfigService.getTableNameById(tableId);
        String chineseName=this.tableConfigService.getChineseName(tableId);
        String configJson=greatMapper.getDesc(tableName);
        JSONObject jsonObject=JSONObject.fromObject(configJson);
        HashMap<String,Object> map=new HashMap<>();
        map.put("pageName",chineseName);
        map.put("totalSize",this.tableConfigService.getSize(tableId));
        for(Object key:jsonObject.keySet()){
            map.put(key.toString(),jsonObject.get(key).toString());
        }
        String primaryKey = this.tableConfigService.getPrimaryKeyByTableId(tableId);
        boolean isDigit = this.tableConfigService.getColumnType(tableId, primaryKey) != String.class;
        HashMap<String,Object> index = new HashMap<>(2);
        index.put("name",primaryKey);
        index.put("isDigit",isDigit);
        map.put("index", JSONHelper.map2Json(index));
        return map;
    }

    /**
     * 设置更新配置接口,配置参数修改
     * @param request 使用request中的param传递需要修改的配置
     * @param tableId 表的Id（即保存在META_ENTITY中的自增字段）
     * @return true成功 false失败
     */
    @PostMapping(value = "/setConfig/{tableId}")
    public Boolean setConfig(HttpServletRequest request,@PathVariable("tableId")int tableId)
    {
        String tableName=this.tableConfigService.getTableNameById(tableId);
        JSONObject allJson=JSONObject.fromObject(greatMapper.getDesc(tableName));
        JSONObject jsonObject=allJson.getJSONObject("upgrade");
        Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()){
            String key = names.nextElement();
            if(jsonObject.has(key)){
                JSONObject jsonToUpdate = jsonObject.getJSONObject(key);
                jsonToUpdate.put("value",request.getParameter(key));
            }
        }
        try {
            greatMapper.updateDesc(tableName, allJson.toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 公共库展示接口
     * @param tableId 表的ID（即保存在META_ENTITY中的自增字段）
     * @param page 分页
     * @param size 分页
     * @return 返回对应的公共库分页后的数据列表，具体格式见api文档
     */
    @GetMapping(value ="/display/{tableId}")
    public List<HashMap> show(@PathVariable("tableId") int tableId
            ,@RequestParam(value = "page") int page,@RequestParam(value = "size") int size)
    {
        String orderFactor;
        String primaryKey = this.tableConfigService.getPrimaryKeyByTableId(tableId);
        if(this.tableConfigService.getColumnType(tableId,primaryKey) != String.class){
            orderFactor="evaluate_time";
        }
        else{
            orderFactor="PDSJ";
        }
        String tableName=this.tableConfigService.getTableNameById(tableId);
        int start=(page-1)*size;
        List<HashMap> hashMaps;
        try{
            hashMaps=greatMapper.desc(tableName,false,start,size,orderFactor);
        }catch (Exception ignore){
            hashMaps=greatMapper.desc(tableName,false,start,size,"ID");
        }
        return hashMaps;
    }

    /**
     * 通用公共库搜索接口
     * @param jsonObject 搜索条件json对象（键值对格式）
     * @param tableId 表的ID（即保存在META_ENTITY中的自增字段）
     * @param page  分页
     * @param size  分页
     * @return  返回搜索结果数据列表
     */
    @PostMapping(value = "/freeSearch/{tableId}")
    public List<HashMap> freeSearch(@RequestBody JSONObject jsonObject,@PathVariable("tableId") int tableId
                                    ,@RequestParam(value="page",defaultValue = "0") int page,
                                    @RequestParam(value = "size",defaultValue = "0") int size)
    {
        List<HashMap> searchMaps=this.mysqlDataManagerService.conditionSearch(jsonObject,tableId,page,size);
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("totalSize",this.mysqlDataManagerService.conditionCount(jsonObject,tableId));
        searchMaps.add(hashMap);
        return searchMaps;
    }

    @PostMapping(value = "/export/{tableId}")
    public JobInfo export(@PathVariable int tableId, @RequestParam String target, @RequestBody(required = false) JSONObject params) throws Exception {
        if(params == null){
            params = new JSONObject();
        }
        params.put("start",false);
        String[] columns = this.tableConfigService.getColumnNamesOfTable(tableId);
        params.put("headers",Arrays.asList(columns));
        return dataTransmissionService.export(target, tableId, params,this.generateProgressCallBack());
    }

    @PostMapping(value = "/import/{tableId}/from/{type}")
    public JobInfo importData(@PathVariable int tableId,@PathVariable String type,@RequestBody JSONObject params) throws Exception {
        switch (type){
            case "excel":
                String fileName = this.uploadResource(params.getString("file"));
                params.put("file",fileName);
            break;
            default:
            break;
        }
        params.put("tableId",tableId);
        if(!params.containsKey("start")){
            params.put("start",false);
        }
        return dataTransmissionService.importData(type, params, this.generateProgressCallBack());
    }

    private ProcessCallBack generateProgressCallBack(){
        return new ProcessCallBack() {
            private long id;

            @Override
            public void onProcessChange(int currentProcess) {
                jmsMessagingTemplate.convertAndSend("progress_object",
                        this.generateMsg(id,currentProcess,null,null));
            }
            @Override
            public void setProgressId(long progressId) {
                id = progressId;
            }
            @Override
            public void log(String message) {
                jmsMessagingTemplate.convertAndSend("progress_object",
                        this.generateMsg(id,null,null,message));
            }
            @Override
            public void processFinished(Object result) {
                jmsMessagingTemplate.convertAndSend("progress_object",
                        this.generateMsg(id,101,result,null));
            }

            private HashMap<String,Object> generateMsg(long jobId,Integer progress,Object result,String log){
                HashMap<String,Object> msg = new HashMap<>(4);
                msg.put("jobId",jobId);
                if(progress!=null){
                    msg.put("progress",progress);
                }
                if(result !=null){
                    msg.put("result",result);
                }
                if(log !=null){
                    msg.put("log",log);
                }
                return msg;
            }
        };
    }

    @GetMapping(value = "/job/result/{jobId}")
    public Object getJobResult(@PathVariable long jobId){
        return this.threadManagerService.getThreadResult(jobId);
    }

    @GetMapping(value = "/download")
    public ResponseEntity<FileSystemResource> downloadResource(@RequestParam String fileName) throws UnsupportedEncodingException, OperationNotSupportedException {
        return resourceService.download(fileName);
    }

    /**
     * @param fileBase64Format 使用BASE63编码的字符串
     * @return 系统存放该文件的文件名
     */
    @PostMapping(value = "/upload")
    public String uploadResource(@RequestBody String fileBase64Format) throws Exception {
        MultipartFile file = FileHelper.Base64ToMultipartFile(fileBase64Format);
        return resourceService.uploadFile(file);
    }

    @PostMapping(value = "/config/column")
    public List<String> getColName(@RequestBody JSONObject params) throws SQLException, ClassNotFoundException {
        String host = params.getString("host");
        int port = params.getInt("port");
        String schema = params.getString("schema") ;
        String user = params.getString("user");
        String password = params.getString("password");
        String tableName = params.getString("tableName");
        return MysqlHelper.getColumnNameOfTable(MysqlHelper.connectToRemoteMysqlServer(host,port,schema,user,password),tableName);
    }

    @GetMapping(value = "/download/template/excel/{tableId}")
    public ResponseEntity<FileSystemResource> getExcelTemplate(@PathVariable("tableId")int tableId) throws IOException, OperationNotSupportedException {
        Workbook workbook = this.dataTransmissionService.getExcelTemplateForImport(tableId);
        File file = this.resourceService.createTmpFile("xlsx");
        try(FileOutputStream outputStream = new FileOutputStream(file)){
            workbook.write(outputStream);
        }
        return this.resourceService.download(file);
    }

}
