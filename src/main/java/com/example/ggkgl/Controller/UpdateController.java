package com.example.ggkgl.Controller;

import com.example.ggkgl.AssitClass.JSONHelper;
import com.example.ggkgl.Mapper.GreatMapper;
import com.example.ggkgl.Service.*;
import javafx.util.Pair;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
     * 保存更新数据
     * @param tableId 表的Id（即保存在META_ENTITY中的自增字段）
     * @param jsonArray 修改信息json数组
     * @return  true成功 false失败
     */
    @PostMapping(value = "/redis/modify/{tableId}")
    @SuppressWarnings("unchecked")
    public Boolean modifyData(@PathVariable("tableId") int tableId,@RequestBody JSONArray jsonArray)
    {
        List<HashMap> modifyList = new ArrayList<>(jsonArray.size());
        for(int i = 0;i<jsonArray.size();i++){
            modifyList.add(JSONHelper.jsonStr2Map(jsonArray.getString(i)));
        }
        Set<Integer> indexSet = this.spiderDataManagerService.recordModifySpiderData(tableId,modifyList);
        String primaryKey = this.tableConfigService.getPrimaryKeyByTableId(tableId);
        List<HashMap> data = new ArrayList<>(indexSet.size());
        for(int i : indexSet){
            HashMap map = this.spiderDataManagerService.getDataFromSpiderAfterModifying(tableId,null,i);
            HashMap<String,Object> contrastResult = this.dataManagerService.contrast(tableId,map);
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
                opMap.put("value",map);
            }
            data.add(opMap);
        }
        dataManagerService.mysqlDataRetention(tableId,data,true);
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
    @SuppressWarnings("unchecked")
    public JSONObject searchUpgrade(@RequestBody JSONObject jsonObject,@PathVariable("tableId")int tableId,
                                   @RequestParam(value = "page",defaultValue = "0") int page,
                                   @RequestParam(value = "size",defaultValue = "20") int size)
    {
        HashMap<String,Object> filter = new HashMap<>();
        filter.put("page",page);
        filter.put("size",size);
        for(Object key : jsonObject.keySet()){
            filter.put(key.toString(),jsonObject.get(key));
        }
        String currentVersion = this.redisVersionControlService.getCurrentVersion(tableId);
        HashMap contrastResult = this.spiderDataManagerService.getContrastResult(tableId, filter);
        List<HashMap> list = (List<HashMap>)contrastResult.get("result");
        int updateCount=0;
        int newCount=0;
        int sameCount=0;
        for (HashMap map : list) {
            String status = map.get("status").toString();
            switch (status) {
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
        }
        JSONObject summer=new JSONObject();
        summer.put("version",currentVersion);
        summer.put("totalCount",contrastResult.get("totalCount"));
        summer.put("newCount",newCount);
        summer.put("updateCount",updateCount);
        summer.put("sameCount",sameCount);
        JSONObject result = new JSONObject();
        result.put("summer",summer);
        result.put("detail",list);
        return result;
    }


}
