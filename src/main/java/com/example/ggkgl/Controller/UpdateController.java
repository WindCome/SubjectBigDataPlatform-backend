package com.example.ggkgl.Controller;

import com.example.ggkgl.AssitClass.JSONHelper;
import com.example.ggkgl.AssitClass.ProcessCallBack;
import com.example.ggkgl.Model.LogInfoEntity;
import com.example.ggkgl.Service.*;
import javafx.util.Pair;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 包含所有更新相关的接口和搜索接口
 */
@RestController
@CrossOrigin
public class UpdateController {
    private final SpiderService spiderService;

    private final RedisVersionControlService redisVersionControlService;

    @Resource
    private MysqlDataManagerService mysqlDataManagerService;

    @Resource
    private TableConfigService tableConfigService;

    @Resource
    private RedisDataManagerService redisDataManagerService;

    @Resource
    private ResourceService resourceService;

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
        String redisKey = this.redisDataManagerService.getSpiderDataRedisKey(tableId);
        String currentVersion = this.redisVersionControlService.getCurrentVersion(redisKey);
        return this.redisVersionControlService.getIndexChangeDetail(redisKey,version,currentVersion);
    }

    /**
     * 修改爬虫数据
     * @param tableId 表的Id（即保存在META_ENTITY中的自增字段）
     * @param index  该数据在爬虫数据列表中的下标
     * @param jsonObject 修改信息json数组修改信息格式如下:
     *                           {"id": Object (可缺省的,mysql记录的主键值，用于指明该爬虫信息用于更新哪条mysql记录),
     *                           "value":Map (不可缺省的,爬虫数据修改后的值)}
     * @return  修改结果信息{"key":String ("success"或者"fail"),"value":String (成功时为空字符串,失败时为报错信息)}
     */
    @PostMapping(value = "/spider/update/{tableId}")
    @SuppressWarnings("unchecked")
    public Pair<String,String> updateSpiderData(@PathVariable("tableId") int tableId,@RequestParam int index,
                                                @RequestBody JSONObject jsonObject)
    {
        jsonObject.put("op", RedisDataManagerService.OperatorCode.UPDATE);
        jsonObject.put("index",index);
        String redisKey = this.redisDataManagerService.getSpiderDataRedisKey(tableId);
        Pair<Boolean,Object> ans = this.redisVersionControlService.recordModifyRedisData(redisKey,JSONHelper.json2Map(jsonObject));
        return this.handleModifyResult(ans,redisKey,index);
    }

    /**
     * 恢复已被删除的爬虫数据
     * @param tableId 表的Id（即保存在META_ENTITY中的自增字段）
     * @param index 该数据在爬虫数据列表中的下标
     * @return  修改结果信息,格式如下:
     *      {"key":String ("success"或者"fail"),"value":String (成功时为恢复后的对比结果json字符串,失败时为报错信息)}
     */
    @GetMapping(value = "/spider/reset/{tableId}")
    @SuppressWarnings("unchecked")
    public Pair<String,String> resetSpiderData(@PathVariable("tableId") int tableId,@RequestParam("index") int index)
    {
        HashMap params = new HashMap(2);
        params.put("op", RedisDataManagerService.OperatorCode.RESET);
        params.put("index",index);
        String redisKey = this.redisDataManagerService.getSpiderDataRedisKey(tableId);
        return this.handleModifyResult(this.redisVersionControlService.recordModifyRedisData(redisKey,params),
                                            redisKey,index);
    }

    /**
     * 删除爬虫数据
     * @param tableId 表的Id（即保存在META_ENTITY中的自增字段）
     * @param index 该数据在爬虫数据列表中的下标
     * @return  修改结果信息{"key":String ("success"或者"fail"),"value":String (成功时为空字符串,失败时为报错信息)}
     */
    @DeleteMapping(value = "/spider/delete/{tableId}")
    @SuppressWarnings("unchecked")
    public Pair<String,String> deleteSpiderData(@PathVariable("tableId") int tableId,@RequestParam("index") int index)
    {
        HashMap params = new HashMap(2);
        params.put("op", RedisDataManagerService.OperatorCode.DELETE);
        params.put("index",index);
        String redisKey = this.redisDataManagerService.getSpiderDataRedisKey(tableId);
        return this.handleModifyResult(this.redisVersionControlService.recordModifyRedisData(redisKey,params),
                                            redisKey,index);
    }

    private Pair<String,String> handleModifyResult(Pair<Boolean,Object> ans,String redisKey,int index){
        if(ans.getKey()){
            String detail = "";
            if(ans.getValue().equals(true)){
                detail = JSONHelper.map2Json(this.redisDataManagerService.getContrastResult(redisKey,index));
            }
            return new Pair<>("success",detail);
        }
        return new Pair<>("fail",ans.getValue().toString());
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
     * @param indexList 爬虫数据下标数组,null时保存全部
     * @return  true成功 false失败
     */
    @PostMapping(value = "/redis/save/{tableId}")
    @SuppressWarnings("unchecked")
    public boolean saveRedisData(@PathVariable("tableId") int tableId,@RequestBody List<Integer> indexList,ProcessCallBack processCallBack)
    {
        if(indexList == null){
            String redisKey = this.redisDataManagerService.getSpiderDataRedisKey(tableId);
            int size = this.redisDataManagerService.getSizeOfData(redisKey);
            indexList = IntStream.iterate(0, n -> n + 1).limit(size).boxed().collect(Collectors.toList());
        }else if(indexList.size() == 0){
            return true;
        }
        String primaryKey = this.tableConfigService.getPrimaryKeyByTableId(tableId);
        List<HashMap> data = new ArrayList<>(indexList.size());
        for(int i : indexList){
            String redisKey = this.redisDataManagerService.getSpiderDataRedisKey(tableId);
            HashMap<String,Object> contrastResult = this.redisDataManagerService.getContrastResult(redisKey,i);
            String status = contrastResult.get("status").toString();
            HashMap<String,Object> opMap = new HashMap<>(3);
            if(status.equals("new")){
                opMap.put("op", MysqlDataManagerService.OperatorCode.NEW);
            }else if(status.equals("update")){
                List<HashMap> similarData = (List<HashMap>)contrastResult.get("data");
                if (similarData.size() == 0){
                    System.out.println("标志为更新的数据项没有更新目标");
                    continue;
                }
                HashMap targetData = similarData.get(0);
                opMap.put("op", MysqlDataManagerService.OperatorCode.DELETE);
                opMap.put("index",targetData.get(primaryKey));
            }
            opMap.put("value",contrastResult.get("oriData"));
            data.add(opMap);
        }
        mysqlDataManagerService.mysqlDataRetention(tableId,data,processCallBack,true);
        return true;
    }

    /**
     * 生成更新数据
     * @param tableId 表的Id（即保存在META_ENTITY中的自增字段）
     * @return 爬虫是否启动成功
     */
    @GetMapping(value = "/generateUpgrade/{tableId}")
    public boolean generate(@PathVariable("tableId") int tableId)
    {
        try{
            this.spiderService.execCrawl(tableId,
                    new SpiderService.CrawlCallBack(){
                        private Object dataDump = null;
                        private String redisKey;
                        @Override
                        public void onStart() {
                            redisKey = RedisDataManagerService.getSpiderDataRedisKey(tableId);
                            this.dataDump = UpdateController.this.redisDataManagerService.getJsonDataList(redisKey);
                        }
                        @Override
                        public void onFinished() {
                            String oldVersion = UpdateController.this.redisVersionControlService.getCurrentVersion(redisKey);
                            if(UpdateController.this.redisVersionControlService.contrast(redisKey,dataDump,
                                    UpdateController.this.redisDataManagerService.getJsonDataList(redisKey))){
                                UpdateController.this.spiderService.onVersionChanged(tableId);
                            }
                            String currentVersion = UpdateController.this.redisVersionControlService.getCurrentVersion(redisKey);
                            List<Pair> changeList = UpdateController.this.redisVersionControlService.getIndexChangeDetail(redisKey,oldVersion,currentVersion);
                            UpdateController.this.redisVersionControlService.resetIndex(redisKey, changeList);
                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 查看某个爬取结果是否可用
     * @param tableId mysql表Id
     * @return  true可用，false不可用
     */
    @GetMapping(value = "/generateUpgrade/result/{tableId}")
    public boolean isDataAvailable(@PathVariable("tableId") int tableId){
        return this.spiderService.isSpiderCrawlFinish(tableId);
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
        String redisKey = this.redisDataManagerService.getSpiderDataRedisKey(tableId);
        String currentVersion = this.redisVersionControlService.getCurrentVersion(redisKey);
        int updateCount=0;
        int newCount=0;
        int sameCount=0;
        int deleteCount = 0;
        String targetType = (String)jsonObject.getOrDefault("status","all");
        HashMap condition = JSONHelper.jsonStr2Map(jsonObject.getOrDefault("condition","").toString());
        int dataSize =  this.redisDataManagerService.getSizeOfData(redisKey);
        List<HashMap> contrastResultList = new ArrayList<>(dataSize);
        int startIndex = (page - 1) * size;
        int coincidentIndex = 0;
        for (int i = 0 ; i < dataSize ; i++){
            HashMap contrastResult = this.redisDataManagerService.getContrastResult(redisKey,i);
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
                case "delete":
                    deleteCount++;
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
        summer.put("deleteCount",deleteCount);
        JSONObject result = new JSONObject();
        result.put("summer",summer);
        result.put("detail",contrastResultList);
        return result;
    }

    /**
     * 获取上次爬虫运行Log
     * @param tableId  表的Id（即保存在META_ENTITY中的自增字段）
     * @return 日志信息
     */
    @GetMapping(value = "/spider/log/{tableId}")
    public LogInfoEntity getCrawlLog(@PathVariable("tableId")int tableId){
        return this.spiderService.getLastCrawlLog(tableId);
    }

    /**
     * 下载爬虫运行Log
     * @param tableId  表的Id（即保存在META_ENTITY中的自增字段）
     * @return 日志文本
     */
    @GetMapping(value = "/spider/log/download/{tableId}")
    public ResponseEntity<FileSystemResource> downloadCrawlLog(@PathVariable("tableId")int tableId) throws IOException, OperationNotSupportedException {
        LogInfoEntity log = this.spiderService.getLastCrawlLog(tableId);
        List<String> logContent = new ArrayList<>();
        logContent.add("运行于:"+log.getGenerateAtTime());
        logContent.add("爬取耗时: "+log.getSpendTime()+"ms");
        logContent.add("详情:");
        logContent.addAll(log.getDetailInfo());
        final String fileName = "spiderLog-"+System.currentTimeMillis()+".txt";
        this.resourceService.saveFile(logContent,fileName);
        return resourceService.download(fileName);
    }
}
