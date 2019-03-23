package com.example.ggkgl.Service;

import com.example.ggkgl.AssitClass.ExceptionHelper;
import com.example.ggkgl.Mapper.LogRepository;
import com.example.ggkgl.Model.LogInfoEntity;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Thread.State.TERMINATED;

/**
 * 控制爬虫相关
 */
@Service
public class SpiderService {
    /**
     * 默认爬虫所在目录
     */
    private static final String DEFAULT_SPIDERS_PATH = "D:\\Project\\ConfigurableSpiders\\ConfigurableSpiders";

    private ConcurrentHashMap<Integer,Thread> spiderThreads = new ConcurrentHashMap<>(60);

    private final Logger logger = Logger.getLogger(SpiderService.class);

    @Resource
    private TableConfigService tableConfigService;

    @Resource
    private LogRepository logRepository;

    public interface CrawlCallBack {
        /**
         * 爬虫进程开始前被调用
         */
        void onStart();
        /**
         * 爬虫进程结束后被调用
         */
        void onFinished();
    }

    /**
     * 根据参数启动爬虫进行数据爬取
     * @param tableId   mysql表id
     * @param callBack  爬虫爬取结束回调
     */
    public void execCrawl(int tableId,CrawlCallBack callBack){
        if(!isSpiderCrawlFinish(tableId)){
            return ;
        }
        String execCommand = this.tableConfigService.getSpiderCommand(tableId);
        String spiderPath = this.tableConfigService.getSpiderPath(tableId);
        final String spiderExecPath = spiderPath == null ? SpiderService.DEFAULT_SPIDERS_PATH : spiderPath;
        String watcher = "spiderProcess"+tableId;
        synchronized (watcher.intern()){
            if(!isSpiderCrawlFinish(tableId)){
                return;
            }
            Thread thread = new Thread(() -> {
                List<String> logInfo = new ArrayList<>();
                LogInfoEntity logInfoEntity = new LogInfoEntity(tableId);
                StopWatch stopWatch = new StopWatch();
                try {
                    ProcessBuilder processBuilder = new ProcessBuilder(execCommand.split(" "));
                    processBuilder.directory(new File(spiderExecPath));
                    processBuilder.redirectErrorStream(true);
                    if(callBack!=null){
                        callBack.onStart();
                    }
                    stopWatch.start();
                    Process process = processBuilder.start();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(),"utf-8"));
                    String line;
                    while ((line = bufferedReader.readLine()) != null){
                        SpiderService.this.logger.info(line);
                        logInfo.add(line);
                    }
                    bufferedReader.close();
                    process.waitFor();
                    stopWatch.stop();
                    if(callBack!=null){
                        callBack.onFinished();
                    }
                }
                catch (Exception e)
                {
                    logInfo.add(ExceptionHelper.getExceptionAllInfo(e));
                    e.printStackTrace();
                }
                logInfoEntity.setDetailInfo(logInfo);
                if(stopWatch.getTaskCount()!=0){
                    logInfoEntity.setSpendTime(stopWatch.getLastTaskTimeMillis());
                }
                logRepository.save(logInfoEntity);
            });
            thread.start();
            this.spiderThreads.put(tableId,thread);
        }
    }

    /**
     * 查询爬虫进程是否已经结束
     */
    public boolean isSpiderCrawlFinish(int tableId) {
        return !this.spiderThreads.containsKey(tableId)
                || this.spiderThreads.get(tableId).getState() == TERMINATED;
    }

    public LogInfoEntity getLastCrawlLog(int tableId){
        Optional<LogInfoEntity> optional = this.logRepository.findById(tableId);
        return optional.orElse(null);
    }
}
