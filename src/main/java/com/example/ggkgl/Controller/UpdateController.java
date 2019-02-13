package com.example.ggkgl.Controller;

import com.example.ggkgl.AssitClass.JSONHelper;
import com.example.ggkgl.Mapper.GreatMapper;
import com.example.ggkgl.Service.*;
import javafx.util.Pair;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
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

    private final DataManagerService dataManagerService;

    private final RedisVersionControlService redisVersionControlService;

    @Resource
    private TableConfigService tableConfigService;

    @Resource
    private SpiderDataManagerService spiderDataManagerService;

    @Autowired
    public UpdateController(SpiderService spiderService, DataManagerService dataManagerService, RedisVersionControlService redisVersionControlService) {
        this.spiderService = spiderService;
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
    @SuppressWarnings("unchecked")
    public JSONObject contrast(@PathVariable("tableId") int tableId, @RequestParam("page") int page
            ,@RequestParam("size") int size,@RequestParam(value = "status",defaultValue = "all") String condition)
    {
        String currentVersion = this.redisVersionControlService.getCurrentVersion(tableId);
        List<HashMap> contrastResult = this.spiderDataManagerService.getContrastResult(tableId);
        int newCount=0;
        int updateCount=0;
        int savedCount=0;
        int sameCount=0;
        JSONObject showObject = new JSONObject();
        JSONArray dataArray = new JSONArray();
        for(int i=0,start=(page-1)*size,counter=0;i<contrastResult.size();i++)
        {
            HashMap data = contrastResult.get(i);
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
        summerObject.put("version",currentVersion);
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
     * 获取指定版本至当前最新版本的下标变化情况
     * @param tableId mysql表id
     * @param version 询问的版本
     */
    @GetMapping(value = "/version/{version}/index/{tableId}")
    public List<Pair> getIndexChangeList(@PathVariable("tableId") int tableId,
                                     @PathVariable(value = "version") String version) {
        String currentVersion = this.redisVersionControlService.getCurrentVersion(tableId);
        return this.redisVersionControlService.getIndexChangeDetail(tableId,version,currentVersion);
    }

    /**
     * 保存更新数据
     * @param tableId 表的Id（即保存在META_ENTITY中的自增字段）
     * @param jsonArray 修改信息json数组
     * @return  true成功 false失败
     */
    @PostMapping(value = "/redis/modify/{tableId}")
    public Boolean modifyData(@PathVariable("tableId") int tableId,@RequestBody JSONArray jsonArray)
    {
        List<HashMap> modifyList = new ArrayList<>(jsonArray.size());
        for(int i = 0;i<jsonArray.size();i++){
            modifyList.add(JSONHelper.jsonStr2Map(jsonArray.getString(i)));
        }
        this.spiderDataManagerService.modifySpiderData(tableId,modifyList);
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
        String tableName= this.tableConfigService.getTableNameById(tableId);
        JSONObject allJson=JSONObject.fromObject(greatMapper.getDesc(tableName));
        JSONObject upgradeJson = allJson.getJSONObject("upgrade");
        JSONObject command=upgradeJson.getJSONObject("command");
        return this.spiderService.execCrawl(null, command.getString("value"),
                new SpiderService.CrawlCallBack(){
            private String dataDump = null;
            @Override
            public void onStart() {
                this.dataDump = UpdateController.this.spiderDataManagerService.getJsonDataFromSpider(tableId);
            }
            @Override
            public void onFinished() {
                String oldVersion = UpdateController.this.redisVersionControlService.getCurrentVersion(tableId);
                UpdateController.this.redisVersionControlService.contrast(tableId,dataDump,
                        UpdateController.this.spiderDataManagerService.getJsonDataFromSpider(tableId));
                String currentVersion = UpdateController.this.redisVersionControlService.getCurrentVersion(tableId);
                List<Pair> changeList = UpdateController.this.redisVersionControlService.getIndexChangeDetail(tableId,oldVersion,currentVersion);
                UpdateController.this.spiderDataManagerService.resetIndex(tableId, changeList);
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
