package com.example.ggkgl.Service;

import com.example.ggkgl.Mapper.RecordDetailRepository;
import com.example.ggkgl.Mapper.RecordRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;

/**
* Mysql 版本控制相关
 **/
@Service
public class MysqlVersionControlService {
    @Resource private RecordDetailRepository recordDetailRepository;
    @Resource private RecordRepository recordRepository;
    /**
     * 获取版本变迁的简要说明
     * @return 变迁概要列表 ，数据格式如下
     *          [
     *              {
     *                  "currentVersion":当前版本号，
     *                  "parentVersion":父版本号，多个父版本号时以#分割，
     *                  "updateTime":该组数据的更新时间,
     *                  "newCount":新增数据数量，
     *                  "updateCount":修改数据数量，
     *                  "deleteCount":删除数据的数量
     *              },....{}
     *          ]
     */
    ArrayList<Object> getVersionHistorySummary(){
        return null;
    }

    /**
     * 获取最新的版本号
     */
    String getLatestVersion(){
        return null;
    }
}
