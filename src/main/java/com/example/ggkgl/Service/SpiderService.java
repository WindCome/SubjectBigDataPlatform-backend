package com.example.ggkgl.Service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Thread.State.TERMINATED;

/**
 * 控制爬虫相关,当前爬虫启动参数只支持-a
 */
@Service
public class SpiderService {
    /**
     * 默认爬虫所在目录
     */
    private static final String DEFAULT_SPIDERS_PATH = "D:\\Project\\ConfigurableSpiders\\ConfigurableSpiders";

    private ConcurrentHashMap<Integer,Thread> spiderThreads = new ConcurrentHashMap<>();

    public interface CrawlFinishCallBack{
        void onFinished();
    }

    /**
     * 根据参数启动爬虫进行数据爬取
     * @param spiderPath 爬虫所在目录路径
     * @param execCommand 启动命令
     * @param callBack  爬虫爬取结束回调
     * @return           爬虫线程ID
     */
    public long execCrawl(String spiderPath, String execCommand,CrawlFinishCallBack callBack){
        final String spiderExecPath = spiderPath == null ? SpiderService.DEFAULT_SPIDERS_PATH : spiderPath;
        Thread spiderThread = this.getSpiderThread(spiderPath,execCommand);
        if(spiderThread != null){
            return spiderThread.getId();
        }
        Integer hashKey = Objects.hash(execCommand, spiderPath);
        synchronized (hashKey.toString().intern()){
            if(this.spiderThreads.containsKey(hashKey)){
                return this.spiderThreads.get(hashKey).getId();
            }
            Thread thread = new Thread(() -> {
                try {
                    Process process = Runtime.getRuntime().exec(execCommand, null, new File(spiderExecPath));
                    InputStreamReader in = new InputStreamReader(process.getInputStream(), "GBK");
                    LineNumberReader input = new LineNumberReader(in);
                    String line;
                    while ((line = input.readLine()) != null)
                        System.out.println(line);
                    in.close();
                    process.waitFor();
                    if(callBack!=null){
                        System.out.println("========finished=======");
                        callBack.onFinished();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            });
            thread.start();
            this.spiderThreads.put(hashKey,thread);
        }
        return this.spiderThreads.get(hashKey).getId();
    }

    /**
     * 判断指定爬虫是否已经完成
     * @param spiderPath 爬虫所在目录路径
     * @param execCommand 启动命令
     * @return  爬虫是否已经完成
     */
    public Boolean isSpiderCrawlFinish(String spiderPath, String execCommand) {
        Thread spiderThread = this.getSpiderThread(spiderPath,execCommand);
        return spiderThread != null && spiderThread.getState().equals(TERMINATED);
    }

    private Thread getSpiderThread(String spiderPath, String execCommand){
        if(spiderPath == null){
            spiderPath = SpiderService.DEFAULT_SPIDERS_PATH;
        }
        Integer hashKey = Objects.hash(execCommand,spiderPath);
        if(this.spiderThreads.containsKey(hashKey)){
            return this.spiderThreads.get(hashKey);
        }
        return null;
    }
}
