package com.example.ggkgl.Controller;

import com.example.ggkgl.AssitClass.JSONHelper;
import com.example.ggkgl.AssitClass.ProcessCallBack;
import com.example.ggkgl.Mapper.GreatMapper;
import com.example.ggkgl.Service.*;
import javafx.util.Pair;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 包含所有更新相关的接口和搜索接口
 */
@RestController
@CrossOrigin
public class UpdateController {
    @Resource
    private GreatMapper greatMapper;

    private final SpiderService spiderService;

    private final RedisVersionControlService redisVersionControlService;

    @Resource
    private DataManagerService dataManagerService;

    @Resource
    private TableConfigService tableConfigService;

    @Resource
    private SpiderDataManagerService spiderDataManagerService;

    @Autowired
    public UpdateController(SpiderService spiderService, RedisVersionControlService redisVersionControlService) {
        this.spiderService = spiderService;
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
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status",condition);
        return this.searchUpgrade(jsonObject,tableId,page,size);
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
     * 修改爬虫数据
     * @param tableId 表的Id（即保存在META_ENTITY中的自增字段）
     * @param jsonArray 修改信息json数组修改信息格式如下:
     *                          {"op":String (不可缺省的,"DELETE"或"UPDATE",表示删除或修改),
     *                           "index":Integer (不可缺省的,该数据在爬虫数据列表中的下标),
     *                           "id": Object (可缺省的,mysql记录的主键值，用于指明该爬虫信息用于更新哪条mysql记录),
     *                           "value":Map (不可缺省的,爬虫数据修改后的值)
     *                          }
     * @return  修改成功的下标数组
     */
    @PostMapping(value = "/redis/modify/{tableId}")
    @SuppressWarnings("unchecked")
    public Set<Integer> modifyData(@PathVariable("tableId") int tableId,@RequestBody List<String> jsonArray)
    {
        List<HashMap> modifyList = new ArrayList<>(jsonArray.size());
        for (String aJsonArray : jsonArray) {
            modifyList.add(JSONHelper.jsonStr2Map(aJsonArray));
        }
        return this.redisVersionControlService.recordModifySpiderData(tableId,modifyList);
    }

    /**
     * 保存单条爬虫数据
     * @param tableId mysql表id
     * @param index 爬虫数据下标
     * @return  true成功 false失败
     */
    @PutMapping(value = "/redis/save/{tableId}")
    @SuppressWarnings("unchecked")
    public boolean saveRedisData(@PathVariable("tableId") int tableId,@RequestParam("index") int index){
        return this.saveRedisData(tableId,Collections.singletonList(index),null);
    }

    /**
     * 保存爬虫数据
     * @param tableId mysql表id
     * @param indexList 爬虫数据下标数组,null或者大小为0时保存全部
     * @return  true成功 false失败
     */
    @PostMapping(value = "/redis/save/{tableId}")
    @SuppressWarnings("unchecked")
    public boolean saveRedisData(@PathVariable("tableId") int tableId,@RequestBody List<Integer> indexList,ProcessCallBack processCallBack)
    {
        if(indexList == null){
            int size = this.spiderDataManagerService.getSizeOfData(tableId);
            indexList = IntStream.iterate(0, n -> n + 1).limit(size).boxed().collect(Collectors.toList());
        }
        String primaryKey = this.tableConfigService.getPrimaryKeyByTableId(tableId);
        List<HashMap> data = new ArrayList<>(indexList.size());
        for(int i : indexList){
            HashMap<String,Object> contrastResult = this.spiderDataManagerService.getContrastResult(tableId,i);
            String status = contrastResult.get("status").toString();
            HashMap<String,Object> opMap = new HashMap<>(3);
            if(status.equals("new")){
                opMap.put("op", DataManagerService.OperatorCode.NEW);
            }else if(status.equals("update")){
                List<HashMap> similarData = (List<HashMap>)contrastResult.get("data");
                if (similarData.size() == 0){
                    System.out.println("标志为更新的数据项没有更新目标");
                    continue;
                }
                HashMap targetData = similarData.get(0);
                opMap.put("op", DataManagerService.OperatorCode.DELETE);
                opMap.put("index",targetData.get(primaryKey));
            }
            opMap.put("value",contrastResult.get("oriData"));
            data.add(opMap);
        }
        dataManagerService.mysqlDataRetention(tableId,data,processCallBack,true);
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
            private Object dataDump = null;
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
                UpdateController.this.redisVersionControlService.resetIndex(tableId, changeList);
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
     * @param jsonObject 搜索条件json，具体参见api文档,格式如下:
     *                   {"status":String (对比结果,"same"、"update"、"new"、"all"中的一种,默认为"all"),
         *                "condition":String  (json格式的字典,{"key":"value"})
         *                }
     * @param tableId   表的Id（即保存在META_ENTITY中的自增字段）
     * @param page  分页
     * @param size  分页
     * @return 返回搜索结构列表
     */
    @PostMapping(value = "/searchUpgrade/{tableId}")
    @SuppressWarnings("unchecked")
    public JSONObject searchUpgrade(@RequestBody JSONObject jsonObject,@PathVariable("tableId")int tableId,
                                   @RequestParam(value = "page",defaultValue = "1") int page,
                                   @RequestParam(value = "size",defaultValue = "20") int size)
    {
        String currentVersion = this.redisVersionControlService.getCurrentVersion(tableId);
        int updateCount=0;
        int newCount=0;
        int sameCount=0;
        String targetType = (String)jsonObject.getOrDefault("status","all");
        HashMap condition = JSONHelper.jsonStr2Map(jsonObject.getOrDefault("condition","").toString());
        int dataSize =  this.spiderDataManagerService.getSizeOfData(tableId);
        List<HashMap> contrastResultList = new ArrayList<>(dataSize);
        int startIndex = (page - 1) * size;
        int coincidentIndex = 0;
        for (int i = 0 ; i < dataSize ; i++){
            HashMap contrastResult = this.spiderDataManagerService.getContrastResult(tableId,i);
            if(contrastResult == null){
                //爬虫数据有删除操作
                continue;
            }
            switch (contrastResult.get("status").toString()) {
                case "new":
                    newCount++;
                    break;
                case "update":
                    updateCount++;
                    break;
                case "same":
                    sameCount++;
                    break;
            }
            HashMap map = (HashMap) contrastResult.get("oriData");
            if(condition != null){
                //字段筛选
                boolean match = true;
                for(Object key:condition.keySet()){
                    String conditionStr = condition.get(key).toString().trim();
                    if (!map.containsKey(key) || !map.get(key).toString().contains(conditionStr)){
                        match = false;
                        break;
                    }
                }
                if(!match){
                    continue;
                }
            }
            Assert.notNull(contrastResult,"it's impossible");
            if(targetType.equals("all") || targetType.equals(contrastResult.get("status"))){
                if(coincidentIndex >= startIndex && coincidentIndex < page*size){
                    contrastResult.put("index",i);
                    contrastResultList.add(contrastResult);
                }
                coincidentIndex++;
            }
        }
        JSONObject summer=new JSONObject();
        summer.put("version",currentVersion);
        summer.put("totalCount",coincidentIndex);
        summer.put("newCount",newCount);
        summer.put("updateCount",updateCount);
        summer.put("sameCount",sameCount);
        JSONObject result = new JSONObject();
        result.put("summer",summer);
        result.put("detail",contrastResultList);
        return result;
    }

}
