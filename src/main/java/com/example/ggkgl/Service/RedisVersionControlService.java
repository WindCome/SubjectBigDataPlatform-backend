package com.example.ggkgl.Service;

import com.example.ggkgl.AssitClass.JSONHelper;
import javafx.util.Pair;
import net.sf.json.JSONObject;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* Redis数据版本控制相关
 **/
@Service
public class RedisVersionControlService {
    /**
     * @param tableId 表的Id（即保存在mysql表META_ENTITY中的自增字段）
     * @return redis中用于更新目的mysql表的数据版本号
     */
    public String getCurrentVersion(int tableId){
        Jedis jedis = new Jedis();
        String versionCode = jedis.get("version"+tableId);
        if(versionCode == null){
            versionCode = this.setCurrentVersion(tableId);
        }
        return versionCode;
    }

    /**
     * @param tableId 表的Id（即保存在mysql表META_ENTITY中的自增字段）
     * @param originalData 原本的数据
     * @param currentData 新的数据
     * @return 版本是否发生了变化
     */
    public boolean contrast(int tableId, Object originalData, Object currentData){
        boolean changed = (originalData == null && currentData!=null) ||
                (originalData != null && (currentData == null || !originalData.equals(currentData)));
        if(changed){
            String originalVersion = getCurrentVersion(tableId);
            String currentVersion = setCurrentVersion(tableId);
            if(originalData!=null && currentData!=null){
                List<Pair> detail = this.generateIndexChangeDetail(JSONHelper.jsonStr2MapList(originalData.toString())
                        ,JSONHelper.jsonStr2MapList(currentData.toString()));
                this.saveIndexChange(tableId,originalVersion,currentVersion,detail);
            }
        }
        return changed;
    }

    private void saveIndexChange(int tableId,String fromVersion,String toVersion,List<Pair> detail){
        Jedis jedis = new Jedis();
        Map<String,Object> data = new HashMap<>(2);
        data.put("versions",new Pair<>(fromVersion,toVersion));
        data.put("detail",detail);
        jedis.set("version_change"+tableId, JSONObject.fromObject(data).toString());
    }

    @SuppressWarnings("unchecked")
    public List<Pair> getIndexChangeDetail(int tableId,String fromVersion,String toVersion){
        Jedis jedis = new Jedis();
        Map<String,Object> data = JSONHelper.jsonStr2Map(jedis.get("version_change"+tableId));
        Pair versionInfo = (Pair)data.get("versions");
        if (!versionInfo.getKey().equals(fromVersion) || !versionInfo.getValue().equals(toVersion)){
            return null;
        }
        return (List<Pair>)data.get("detail");
    }

    /**
    * 记录未改动数据的位置变化
     */
    private List<Pair> generateIndexChangeDetail(List<HashMap> originalData,List<HashMap> currentData){
        if(originalData == null || currentData == null){
            return null;
        }
        List<Pair> indexChange = new ArrayList<>();
        for(int i = 0;i < originalData.size() ; i++){
            HashMap map = originalData.get(i);
            int newIndex = currentData.indexOf(map);
            if(newIndex != -1){
                Pair change = new Pair<>(i,newIndex);
                indexChange.add(change);
            }
        }
        return indexChange;
    }

    private synchronized String setCurrentVersion(int tableId){
        Jedis jedis = new Jedis();
        String versionCode = RandomStringUtils.randomAlphanumeric(10);
        jedis.set("version"+tableId, versionCode);
        return versionCode;
    }

}
