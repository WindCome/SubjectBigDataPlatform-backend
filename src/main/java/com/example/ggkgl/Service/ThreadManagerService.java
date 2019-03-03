package com.example.ggkgl.Service;

import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/**
* 线程管理
 **/
@Service
public class ThreadManagerService {
    private ConcurrentHashMap<Long,Thread> threads = new ConcurrentHashMap<>();

    private ConcurrentHashMap<Long,Future> futureMap = new ConcurrentHashMap<>();

    private AtomicLong currentJobId = new AtomicLong(1);

    public Long executeThread(@NotNull Runnable runnable){
        long jobId = this.currentJobId.getAndIncrement();
        if(runnable instanceof Future){
            this.futureMap.put(jobId,(Future)runnable);
        }
        Thread thread = new Thread(runnable);
        thread.start();
        this.threads.put(jobId,thread);
        return jobId;
    }

    public Boolean isThreadTerminated(Long jobId) {
        if(this.threads.containsKey(jobId)){
            return this.threads.get(jobId).getState() == Thread.State.TERMINATED;
        }
        return null;
    }

    public Object getThreadResult(Long jobId){
        if(this.futureMap.containsKey(jobId) && this.futureMap.get(jobId).isDone()){
            try {
                return this.futureMap.get(jobId).get();
            }
            catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void terminateThread(Long jobId){
        if(this.threads.containsKey(jobId)){
            Thread thread = this.threads.get(jobId);
            if(!thread.isInterrupted()){
                this.threads.get(jobId).interrupt();
            }
        }
    }
}
