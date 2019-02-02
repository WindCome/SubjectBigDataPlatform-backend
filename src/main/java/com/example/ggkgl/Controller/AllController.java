package com.example.ggkgl.Controller;

import com.example.ggkgl.AssitClass.Change;
import com.example.ggkgl.AssitClass.JSONHelper;
import com.example.ggkgl.Mapper.GreatMapper;
import com.example.ggkgl.Service.DataManagerService;
import com.example.ggkgl.Service.TableConfigService;
import javafx.util.Pair;
import net.sf.json.JSONObject;
import org.apache.ibatis.annotations.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.JsonObjectDeserializer;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * 包含所有的增删查改等基本接口
 * 所有api文档地址：
 * https://docs.qq.com/doc/BqI21X2yZIht15KNMU0dC08g0ev0tg1N54I42NrF4K1ONwO73IQmKC2Cjyb92O8xq00FpNte0vqpPK1D7MU24?opendocxfrom=admin
 */
@RestController
@CrossOrigin
public class AllController {
    @Autowired
    private GreatMapper greatMapper;

    @Resource
    private DataManagerService dataManagerService;

    @Resource private TableConfigService tableConfigService;

    /**
     * 获取对应公共库的中文名称
     * @param tableId 表的ID（即保存在META_ENTITY中的自增字段）
     * @return 返回对应中文名称
     */
    public String getChineseName(int tableId)
    {
        return greatMapper.freeSearch("META_ENTITY","chinese_name",""+tableId+"");
    }

    /**
     * 获取公共库对应的表的名称
     * @param tableId 表的ID（即保存在META_ENTITY中的自增字段）
     * @return 返回数据库中对应表名
     */
    public String getTableName(int tableId)
    {
        return greatMapper.freeSearch("META_ENTITY","name",""+tableId+"");
    }

    /**
     * 获取公共库表的大小（数据条目数）
     * @param tableId 表的ID（即保存在META_ENTITY中的自增字段）
     * @return 返回大小
     */
    public int getSize(int tableId)
    {
        String tableName=getTableName(tableId);
        return greatMapper.getSize(tableName);
    }

    /**
     * 获取公共库表的类型（主要用于确定表的主键是字符串类型还是Int类型）
     * @param tableId 表的Id（即保存在META_ENTITY中的自增字段）
     * @return  返回类型 1代表Id整型 2代表Id字符串类型
     */
    public int getFlag(int tableId)
    {
        int flag;
        String tableName=getTableName(tableId);
        String jsonStr=greatMapper.getDesc(tableName);
        JSONObject jsonObject=JSONObject.fromObject(jsonStr);
        JSONObject indexObject=jsonObject.getJSONObject("index");
        String isDigit=indexObject.getString("isDigit");
        if(isDigit.equals("true"))
        {
            flag=1;
        }
        else
        {
            flag=2;
        }
        return flag;
    }

    /**
     * 获取所有公共库的列表
     * @param page
     * @param size
     * @return
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
     * @return  {"info":String}             成功时Value为null，失败时Value为出错信息
     */
    @PostMapping(value = "/add/{tableId}")
    public Pair<String,String> add(@PathVariable("tableId") int tableId, @RequestBody JSONObject jsonObject) {
        try{
            HashMap<String,Object> data = new HashMap<>();
            String primaryKey = this.tableConfigService.getPrimaryKeyByTableId(tableId);
            data.put("op", DataManagerService.OperatorCode.NEW);
            data.put("value",jsonObject.toString());
            this.dataManagerService.mysqlDataRetention(tableId, Collections.singletonList(data),true);
            return new Pair<>("info",null);
        }catch (Exception e){
            e.printStackTrace();
            return new Pair<>("info",this.getExceptionAllInfo(e));
        }
    }

    private String getExceptionAllInfo(Exception ex) {
        ByteArrayOutputStream out;
        PrintStream pout = null;
        String ret;
        try {
            out = new ByteArrayOutputStream();
            pout = new PrintStream(out);
            ex.printStackTrace(pout);
            ret = new String(out.toByteArray());
            out.close();
        }
        catch (Exception e) {
            return ex.getMessage();
        }
        finally {
            if (pout != null) {
                pout.close();
            }
        }
        return ret;
    }

    /**
     *获取配置接口
     * @param tableId
     * @return  返回某个公共库的各种配置信息，具体见API文档
     */
    @GetMapping(value = "/getConfig/{tableId}")
    public HashMap displayConfig(@PathVariable("tableId") int tableId)
    {
        String tableName=getTableName(tableId);
        String chineseName=getChineseName(tableId);
        String configJson=greatMapper.getDesc(tableName);
        JSONObject jsonObject=JSONObject.fromObject(configJson);
        HashMap map=new HashMap();
        map.put("totalSize",getSize(tableId));
        map.put("all",jsonObject.get("all").toString());
        map.put("show",jsonObject.get("show").toString());
        map.put("pageName",chineseName);
        map.put("index",jsonObject.get("index").toString());
        map.put("upgrade",jsonObject.get("upgrade").toString());
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
        String tableName=getTableName(tableId);
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
        int flag=getFlag(tableId);
        if(flag==1)
            orderFactor="evaluate_time";
        else
            orderFactor="PDSJ";
        String tableName=getTableName(tableId);
        int start=(page-1)*size;
        List<HashMap> hashMaps;
        try{
            hashMaps=greatMapper.desc(tableName,start,size,orderFactor);
        }catch (Exception ignore){
            hashMaps=greatMapper.desc(tableName,start,size,"ID");
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
                                    ,@RequestParam(value="page",defaultValue = "-1") int page,
                                    @RequestParam(value = "size",defaultValue = "-1") int size)
    {
        String tableName=getTableName(tableId);
        List<Change> changes=new ArrayList<>();
        for(Object key:jsonObject.keySet())
        {
            Change change=new Change();
            change.setKey(key.toString());
            change.setValue(jsonObject.get(key).toString());
            changes.add(change);
        }
        HashMap map=new HashMap();
        map.put("conditions",changes);
        map.put("tableName",tableName);
        List<HashMap> resultMaps=new ArrayList<>();
        List<HashMap> searchMaps=greatMapper.comboSearch(map);
        HashMap hashMap=new HashMap();
        hashMap.put("totalSize",searchMaps.size());
        if(page==-1||size==-1)
        {
            searchMaps.add(hashMap);
            return searchMaps;
        }
        else
        {
            int start=(page-1)*size;
            int end=start+size;
            for(int j=start;j<searchMaps.size()&&j<end;j++)
            {
                resultMaps.add(searchMaps.get(j));
            }
            resultMaps.add(hashMap);
            return resultMaps;
        }
    }

    /**
     * 统一数据修改接口
     * @param tableId 表的ID（即保存在META_ENTITY中的自增字段）
     * @param id 修改数据的Id（即数据库中主键）
     * @param jsonObject 修改json对象（键值对格式），具体参照api文档
     * @return
     */
   @PostMapping(value = "/update/{tableId}")
    public Pair<String,String> update(@PathVariable("tableId") int tableId,@RequestParam(value = "Id") String id,
           @RequestBody JSONObject jsonObject)
   {
       try{
           HashMap<String,Object> data = new HashMap<>();
           data.put("op", DataManagerService.OperatorCode.UPDATE);
           data.put("index",id);
           data.put("value",jsonObject.toString());
           this.dataManagerService.mysqlDataRetention(tableId, Collections.singletonList(data),true);
           return new Pair<>("info",null);
       }catch (Exception e){
           e.printStackTrace();
           return new Pair<>("info",this.getExceptionAllInfo(e));
       }
   }

    /**
     * 统一删除接口
     * @param id 删除数据的Id
     * @param tableId 表的Id（即保存在META_ENTITY中的自增字段）
     * @return true成功 false失败
     */
    @GetMapping(value = "/delete/{tableId}")
    public Pair<String,String> delete(@RequestParam("Id") String id,@PathVariable("tableId") int tableId)
    {
        try{
            HashMap<String,Object> data = new HashMap<>();
            data.put("op", DataManagerService.OperatorCode.DELETE);
            data.put("index",id);
            this.dataManagerService.mysqlDataRetention(tableId, Collections.singletonList(data),true);
            return new Pair<>("info",null);
        }catch (Exception e){
            e.printStackTrace();
            return new Pair<>("info",this.getExceptionAllInfo(e));
        }
    }

}
