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
        if(spiderThread != null && !this.isSpiderCrawlFinish(spiderThread.getId())){
            return spiderThread.getId();
        }
        Integer hashKey = Objects.hash(execCommand, spiderPath);
        synchronized (hashKey.toString().intern()){
            if(this.spiderThreads.containsKey(hashKey)){
                long tid = this.spiderThreads.get(hashKey).getId();
                if(!this.isSpiderCrawlFinish(tid)){
                    return tid;
                }
            }
            Thread thread = new Thread(() -> {
                try {
                    System.out.println("===star crawl=====");
                    ProcessBuilder processBuilder = new ProcessBuilder(execCommand.split(" "));
                    processBuilder.directory(new File(spiderExecPath));
                    processBuilder.redirectErrorStream(true);
                    Process process = processBuilder.start();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(),"utf-8"));
                    String line;
                    while ((line = bufferedReader.readLine()) != null)
                        System.out.println(line);
                    bufferedReader.close();
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
     * 查询爬虫进程是否已经结束
     * @param tid 爬虫线程ID
     * @return  true爬虫线程已经结束，false爬虫正在进行,null不存在该爬虫线程
     */
    public Boolean isSpiderCrawlFinish(long tid) {
        Boolean result = null;
        for(Thread thread :this.spiderThreads.values()){
            if(thread.getId() == tid ){
                Thread.State state = thread.getState();
                result = state == TERMINATED;
            }
        }
        return result;
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
