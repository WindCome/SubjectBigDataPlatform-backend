package com.example.ggkgl.Service;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

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
            setCurrentVersion(tableId);
        }
        return changed;
    }

    private synchronized String setCurrentVersion(int tableId){
        Jedis jedis = new Jedis();
        String versionCode = RandomStringUtils.randomAlphanumeric(10);
        jedis.set("version"+tableId, versionCode);
        return versionCode;
    }

}
