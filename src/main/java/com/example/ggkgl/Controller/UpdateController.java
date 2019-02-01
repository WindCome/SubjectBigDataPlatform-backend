package com.example.ggkgl.Controller;

import com.csvreader.CsvReader;
import com.example.ggkgl.Mapper.GreatMapper;
import com.example.ggkgl.Service.DataManagerService;
import com.example.ggkgl.Service.RedisVersionControlService;
import com.example.ggkgl.Service.SpiderService;
import javafx.util.Pair;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.util.*;

/**
 * 包含所有更新相关的接口和搜索接口
 */
@RestController
@CrossOrigin
public class UpdateController {
    @Resource
    private GreatMapper greatMapper;

    private final SpiderService spiderService;

    private final AllController allController;

    private final DataManagerService dataManagerService;

    private final RedisVersionControlService redisVersionControlService;

    @Autowired
    public UpdateController(SpiderService spiderService, AllController allController, DataManagerService dataManagerService, RedisVersionControlService redisVersionControlService) {
        this.spiderService = spiderService;
        this.allController = allController;
        this.dataManagerService = dataManagerService;
        this.redisVersionControlService = redisVersionControlService;
    }

    /**
     * 获取更新列表
     * @param tableId 表的Id（即保存在META_ENTITY中的自增字段）
     * @param page 分页
     * @param size  分页
     * @param condition 筛选状态（具体见api文档）
     * @return 返回更新数据列表
     */
    @GetMapping(value = "/upgrade/{tableId}")
    public JSONObject contrast(@PathVariable("tableId") int tableId, @RequestParam("page") int page
            ,@RequestParam("size") int size,@RequestParam(value = "status",defaultValue = "all") String condition)
    {
        Pair<String,List<HashMap>> dataEntry = this.dataManagerService.getDataFromSpider(tableId);
        if (dataEntry==null){
            return null;
        }
        List<HashMap> dataList = dataEntry.getValue();
        List<HashMap<String,Object>> contrastResult =new ArrayList<>(dataList.size());
        for (HashMap map : dataList) {
            contrastResult.add(this.dataManagerService.contrast(tableId,map));
        }
        int newCount=0;
        int updateCount=0;
        int savedCount=0;
        int sameCount=0;
        JSONObject showObject = new JSONObject();
        JSONArray dataArray = new JSONArray();
        for(int i=0,start=(page-1)*size,counter=0;i<contrastResult.size();i++)
        {
            HashMap<String,Object> data = contrastResult.get(i);
            String status=data.get("status").toString();
            if(i>=start&&counter<size) {
                if(condition.equals("all")||condition.equals(status)) {
                   data.put("index",i);
                   dataArray.add(data);
                   counter++;
                }
            }
            switch (status) {
                case "new":
                    newCount++;
                    break;
                case "update":
                    updateCount++;
                    break;
                case "saved":
                    savedCount++;
                    break;
                case "same":
                    sameCount++;
                    break;
            }
        }
        JSONObject summerObject=new JSONObject();
        summerObject.put("version",dataEntry.getKey());
        summerObject.put("totalCount",contrastResult.size());
        summerObject.put("newCount",newCount);
        summerObject.put("updateCount",updateCount);
        summerObject.put("savedCount",savedCount);
        summerObject.put("sameCount",sameCount);
        showObject.put("summer",summerObject);
        showObject.put("detail",dataArray);
        return showObject;
    }

    /**
     * 保存单条更新数据
     * @param Index 更新数据的index
     * @param tableId 表的Id（即保存在META_ENTITY中的自增字段）
     * @return  true成功 false失败
     */
    @GetMapping(value = "/upgradeSave/{tableId}")
    public  Boolean upgradeSave(@RequestParam("index") int Index,@PathVariable("tableId") int tableId)
    {
        Jedis jedis=new Jedis();
        String jsonStr=jedis.get("upgrade"+tableId);
        JSONArray jsonArray=JSONArray.fromObject(jsonStr);
        JSONObject jsonObject=jsonArray.getJSONObject(Index);
        String status=jsonObject.getString("status");
        jsonObject.put("status","saved");
        JSONObject dataObject=jsonObject.getJSONObject("data");
        JSONObject addObject=new JSONObject();
        for (Object key: dataObject.keySet().toArray())
        {
            addObject.put(key,dataObject.getJSONObject(key.toString()).getString("newValue"));
        }
        Boolean result=false;
        if(status.equals("new")) {
            result = allController.add(tableId, addObject);
        }
        else if(status.equals("update"))
        {
            String Id=jsonObject.getString("Id");
            result= allController.update(tableId,Id,addObject);
        }
        System.out.println(result);
        if (result)
        {
            jedis.set("upgrade"+tableId,jsonArray.toString());
        }
        return result;
    }

    /**
     * 修改某条更新数据
     * @param Index 更新数据index
     * @param tableId  表的Id（即保存在META_ENTITY中的自增字段）
     * @param changes  更新数据json具体格式样例见api文档
     * @return true成功 false失败
     */
    @PostMapping(value = "/upgrade/modify/{tableId}")
    public Boolean modify(@RequestParam("index") int Index,@PathVariable("tableId") int tableId,
                       @RequestBody JSONObject changes)
    {
        Jedis jedis=new Jedis();
        String jsonStr=jedis.get("upgrade"+tableId);
        JSONArray jsonArray=JSONArray.fromObject(jsonStr);
        JSONObject jsonObject=jsonArray.getJSONObject(Index);
        JSONObject dataObject=jsonObject.getJSONObject("data");
        for (Object key:changes.keySet())
        {
            try {
                JSONObject columnObject = dataObject.getJSONObject(key.toString());
                columnObject.put("newValue", changes.get(key));
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return false;
            }
        }
        jedis.set("upgrade"+tableId,jsonArray.toString());
        return true;
    }

    /**
     * 删除某条更新数据
     * @param tableId 表的Id（即保存在META_ENTITY中的自增字段）
     * @param index 更新数据的index
     * @return true成功，false失败
     */
    @GetMapping(value = "/upgrade/delete/{tableId}")
    public Boolean delete(@PathVariable("tableId")int tableId,@RequestParam("index")int index)
    {
        Jedis jedis=new Jedis();
        String jsonStr=jedis.get("upgrade"+tableId);
        JSONArray jsonArray=JSONArray.fromObject(jsonStr);
        try {
            jsonArray.remove(index);
        }catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        jedis.set("upgrade"+tableId,jsonArray.toString());
        return true;
    }

    /**
     * 生成更新数据
     * @param tableId 表的Id（即保存在META_ENTITY中的自增字段）
     * @return  爬虫线程id
     */
    @GetMapping(value = "/generateUpgrade/{tableId}")
    public long generate(@PathVariable("tableId") int tableId)
    {
        String tableName= allController.getTableName(tableId);
        JSONObject allJson=JSONObject.fromObject(greatMapper.getDesc(tableName));
        JSONObject upgradeJson = allJson.getJSONObject("upgrade");
        JSONObject command=upgradeJson.getJSONObject("command");
        return this.spiderService.execCrawl(null, command.getString("value"),
                new SpiderService.CrawlCallBack(){
            private String dataDump = null;
            private Jedis jedis = new Jedis();
            @Override
            public void onStart() {
                this.dataDump = jedis.get("upgrade"+tableId);
            }
            @Override
            public void onFinished() {
                UpdateController.this.redisVersionControlService.contrast(
                        tableId,dataDump,jedis.get("upgrade"+tableId));
            }
        });
    }

    /**
     * 查看某个爬取结果是否可用
     * @param tid 生成更新数据接口返回的线程id
     * @return  true可用  false不可用
     */
    @GetMapping(value = "/generateUpgrade/result/{tid}")
    public Boolean isDataAvailable(@PathVariable("tid") int tid){
        return this.spiderService.isSpiderCrawlFinish(tid);
    }

    /**
     * 转换读取csv得到的数据map为最后需要的对比后的更新数据map，此方法目前仅被generate调用
     * @param tableName 表名
     * @param map  读取csv得到的map
     * @param year  更新数据的年份
     * @param status  对比后的状态
     * @param index 数据的主键
     * @param sign  对应公共库表的类型
     * @return 返回更新数据map
     */
    public HashMap convertMap(String tableName,HashMap map,String year,String status,String index,int sign)
    {
        HashMap resultMap=new HashMap();
        Boolean flag=true;
        HashMap dataMap=new HashMap();
        String fakeIndex="'"+index+"'";
        for (Object key : map.keySet()) {
            HashMap columnMap = new HashMap();
            String newValue=map.get(key).toString();
            columnMap.put("newValue",newValue == null?"":newValue);
            if(status.equals("update")) {
                String oldValue=greatMapper.freeSearch(tableName,key.toString(),fakeIndex);
                columnMap.put("oldValue",oldValue == null?"":oldValue);
                if(newValue.equals(oldValue))
                    columnMap.put("status","same");
                else {
                    columnMap.put("status", "update");
                    flag=false;
                }
            }
            else {
                columnMap.put("oldValue", "");
                columnMap.put("status","");
                flag=false;
            }
            dataMap.put(key,columnMap);
        }
        if(sign==2) {
            HashMap yearMap = new HashMap();
            yearMap.put("newValue", year);
            if (status.equals("update")) {
                String oldYear = greatMapper.freeSearch(tableName, "PDSJ", fakeIndex);
                yearMap.put("oldValue", oldYear==null?"":oldYear);
                if (oldYear!=null && oldYear.equals(year)) {
                    yearMap.put("status", "same");
                } else {
                    yearMap.put("status", "update");
                    flag = false;
                }
            } else {
                yearMap.put("oldValue", "");
                yearMap.put("status", "");
            }
            dataMap.put("PDSJ", yearMap);
        }
        resultMap.put("Id",index);
        resultMap.put("data",dataMap);
        if(flag)
            resultMap.put("status","same");
        else
            resultMap.put("status",status);
        return resultMap;
    }

    /**
     * 工具类，用于读取csv转换成hashmap
     * @param pathName csv相对路径
     * @return  返回转换后的map
     */
    public List<HashMap> csvToHashMap(String pathName)
    {
        List<HashMap> maps=new ArrayList<>();
        try
        {
            CsvReader csvReader=new CsvReader(pathName,',',Charset.forName("utf-8"));
            csvReader.readRecord();
            String[] titles=csvReader.getValues();
//            for (String title: titles
//                 ) {
//                System.out.println(title);
//            }
            while(csvReader.readRecord())
            {
                HashMap map=new HashMap();
                String[] columns=csvReader.getValues();
                for(int i=0;i<titles.length;i++)
                {
                    map.put(titles[i],columns[i]);
                }
                maps.add(map);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        return maps;
    }

    /**
     * 搜索更新接口
     * @param jsonObject 搜索条件json，具体参见api文档
     * @param tableId   表的Id（即保存在META_ENTITY中的自增字段）
     * @param page  分页
     * @param size  分页
     * @return 返回搜索结构列表
     */
    @PostMapping(value = "/searchUpgrade/{tableId}")
    public JSONArray searchUpgrade(@RequestBody JSONObject jsonObject,@PathVariable("tableId")int tableId,
                                   @RequestParam(value = "page",defaultValue = "-1") int page,
                                   @RequestParam(value = "size",defaultValue = "-1") int size)
    {
        Jedis jedis=new Jedis();
        String jsonstr=jedis.get("upgrade"+tableId);
        JSONArray upgradeJson=JSONArray.fromObject(jsonstr);
        JSONArray resultArray=new JSONArray();
        int savedCount=0;
        int updateCount=0;
        int newCount=0;
        int sameCount=0;
        for(int i=0;i<upgradeJson.size();i++)
        {
            JSONObject item=upgradeJson.getJSONObject(i);
            JSONObject dataObject=item.getJSONObject("data");
            String status=item.getString("status");
            Boolean isMatch=true;
            JSONObject condition=jsonObject.getJSONObject("condition");
            JSONObject statusObject=jsonObject.getJSONObject("status");
            if (statusObject.getString(status).equals("false"))
                isMatch=false;
            for(Object key:condition.keySet())
            {
                String value1=dataObject.getJSONObject(key.toString()).getString("newValue");
                String value2=condition.get(key).toString();
//                System.out.println(value1);
//                System.out.println(value2);
                if(!value1.contains(value2))
                {
                    System.out.println("!");
                    isMatch=false;
                    break;
                }
            }
            if(isMatch) {
                if(status.equals("new"))
                    newCount++;
                else if (status.equals("update"))
                    updateCount++;
                else if(status.equals("saved"))
                    savedCount++;
                else if (status.equals("same"))
                    sameCount++;
                item.put("index",i);
                resultArray.add(item);
            }
        }
        JSONObject total=new JSONObject();
        total.put("totalCount",resultArray.size());
        total.put("newCount",newCount);
        total.put("updateCount",updateCount);
        total.put("savedCount",savedCount);
        total.put("sameCount",sameCount);
        if(page==-1||size==-1)
        {
            resultArray.add(total);
            return resultArray;
        }
        JSONArray returnArray=new JSONArray();
        int start=(page-1)*size;
        int end=start+size;
        for(int i=start;i<resultArray.size()&&i<end;i++)
        {
            returnArray.add(resultArray.getJSONObject(i));
        }
        returnArray.add(total);
        return returnArray;
    }


}
