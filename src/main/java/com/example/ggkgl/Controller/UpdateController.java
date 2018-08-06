package com.example.ggkgl.Controller;

import com.csvreader.CsvReader;
import com.example.ggkgl.AssitClass.Change;
import com.example.ggkgl.Mapper.GreatMapper;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 包含所有更新相关的接口和搜索接口
 */
@RestController
@CrossOrigin
public class UpdateController {
    @Autowired
    private GreatMapper greatMapper;

    @Autowired
    AllController allController;
//    public void execCrawl()
//    {
//        try
//        {
//            Process process=Runtime.getRuntime().exec("activate && F:\\大三下学习\\YangtzeRiverScholar-master\\YangtzeRiverScholar\\run.py");
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//    }

    /**
     * 获取更新列表
     * @param tableId 表的Id（即保存在META_ENTITY中的自增字段）
     * @param page 分页
     * @param size  分页
     * @param condition 筛选状态（具体见api文档）
     * @return 返回更新数据列表
     */
    @GetMapping(value = "/upgrade/{tableId}")
    public JSONArray contrast(@PathVariable("tableId") int tableId, @RequestParam("page") int page
            ,@RequestParam("size") int size,@RequestParam(value = "status",defaultValue = "all") String condition)
    {
        int start=(page-1)*size;
        int end=start+size;
        Jedis jedis=new Jedis();
        String jsonstr=jedis.get("upgrade"+tableId);
        System.out.println(jsonstr);
        if (jsonstr==null)
        {
            return null;
        }
        JSONArray jsonArray=new JSONArray();
        jsonArray = JSONArray.fromObject(jsonstr);
        JSONArray showArray=new JSONArray();
        int newCount=0;
        int updateConut=0;
        int savedCount=0;
        int sameCount=0;
        for(int i=0;i<jsonArray.size();i++)
        {
//            System.out.println(i);
//            System.out.println(jsonArray.get(i));
            String status=jsonArray.getJSONObject(i).getString("status");
            if(i>=start&&i<end) {
                if(condition.equals("all")||condition.equals(status)) {
                   JSONObject item=jsonArray.getJSONObject(i);
                   item.put("index",i);
                    showArray.add(item);
                }
                else
                    end++;
            }
            if(status.equals("new"))
            {
                newCount++;
            }
            else if(status.equals("update"))
            {
                updateConut++;
            }
            else if(status.equals("saved"))
            {
                savedCount++;
            }
            else if(status.equals("same"))
            {
                sameCount++;
            }
        }
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("totalCount",jsonArray.size());
        jsonObject.put("newCount",newCount);
        jsonObject.put("updateCount",updateConut);
        jsonObject.put("savedCount",savedCount);
        jsonObject.put("sameCount",sameCount);
        showArray.add(jsonObject);
        return showArray;
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
     * @param year 更新数据的年份
     * @return  true成功  false失败
     */
    @GetMapping(value = "/generateUpgrade/{tableId}")
    public Boolean generate(@PathVariable("tableId") int tableId,@RequestParam("year") String year)
    {
        int flag= allController.getFlag(tableId);
        String tableName= allController.getTableName(tableId);
        JSONObject allJson=JSONObject.fromObject(greatMapper.getDesc(tableName));
        JSONObject configJson=allJson.getJSONObject("config");
        String fileName=configJson.getString("fileName");
        List<HashMap> maps=csvToHashMap(fileName);
        List<HashMap> ResultMaps=new ArrayList<>();
        int count=0;
        for(int i=0;i<maps.size();i++)
        {
            HashMap item=maps.get(i);
            String mainKey=configJson.getString("mainKey");
            String mainValue=item.get(mainKey).toString();
//            System.out.println(mainKey);
//            System.out.println(mainValue);
            List<HashMap> mainMap=greatMapper.freeInspect(tableName,mainKey,mainValue);
            HashMap map=new HashMap();
            if(mainMap.size()==0)
            {
                count++;
                map=convertMap(tableName,item,year,"new","-1",flag);
            }
            else {
                HashMap conditionMap=new HashMap();
                conditionMap.put("tableName",tableName);
                JSONArray jsonArray=configJson.getJSONArray("matchKeys");
                List<Change> changes=new ArrayList<>();
                Change mainChange=new Change();
                mainChange.setKey(mainKey);
                mainChange.setValue(mainValue);
                changes.add(mainChange);
                for(int j=0;j<jsonArray.size();j++)
                {
                    Change change=new Change();
                    String key=jsonArray.get(j).toString();
                                        System.out.println(key);
                    String value=item.get(key).toString();
                    change.setKey(key);
                    change.setValue(value);

//                    System.out.println(value);
                    changes.add(change);
                }
                conditionMap.put("conditions",changes);
                List<HashMap> specificMap=greatMapper.comboSearch(conditionMap);
//                System.out.println(specificMap.size());
//                System.out.println(item.get("XM"));
                if(specificMap.size()==0) { 
                    map=convertMap(tableName,item,year,"new","-1",flag);
                }
                else if (specificMap.size()==1)
                {
                    String index="";
                    if(flag==1) {
                         index = specificMap.get(0).get("id").toString();
                    }
                    else
                    {
                        index =specificMap.get(0).get("ID").toString();
                    }
                    map=convertMap(tableName,item,year,"update",index,flag);
                }
                else
                {
                    //let it go!!!
                    System.out.println("that is terrible!");
                }
            }
            ResultMaps.add(map);
        }
        System.out.println(count);
        HashMap countMap=new HashMap();
        JSONArray jsonArray=JSONArray.fromObject(ResultMaps);
        try {
            Jedis jedis = new Jedis();
            jedis.set("upgrade" + tableId, jsonArray.toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
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
            columnMap.put("newValue",newValue);
            if(status.equals("update")) {
                String oldValue=greatMapper.freeSearch(tableName,key.toString(),fakeIndex);
                columnMap.put("oldValue",oldValue);
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
                yearMap.put("oldValue", oldYear);
                if (oldYear.equals(year)) {
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
