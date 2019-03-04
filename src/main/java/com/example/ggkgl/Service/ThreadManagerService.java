package com.example.ggkgl.Service;

import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
* 线程管理
 **/
@Service
public class ThreadManagerService {
    private ConcurrentHashMap<Long,Thread> threads = new ConcurrentHashMap<>();

    private ConcurrentHashMap<Long,Future> futureMap = new ConcurrentHashMap<>();

    public Long executeThread(@NotNull Runnable runnable){
        long threadId = this.submitThread(runnable);
        return this.executeThread(threadId);
    }

    public Long executeThread(long threadId){
        Thread thread = this.getThreadById(threadId);
        thread.start();
        return thread.getId();
    }

    public Long submitThread(@NotNull Runnable runnable){
        Thread thread = new Thread(runnable);
        long threadId = thread.getId();
        if(runnable instanceof Future){
            this.futureMap.put(threadId,(Future)runnable);
        }
        this.threads.put(threadId,thread);
        return thread.getId();
    }

    public Thread getThreadById(long threadId){
        return this.threads.getOrDefault(threadId,null);
    }

    public Boolean isThreadTerminated(long threadId) {
        if(this.threads.containsKey(threadId)){
            return this.threads.get(threadId).getState() == Thread.State.TERMINATED;
        }
        return null;
    }

    public Object getThreadResult(long threadId){
        if(this.futureMap.containsKey(threadId) && this.futureMap.get(threadId).isDone()){
            try {
                return this.futureMap.get(threadId).get();
            }
            catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void terminateThread(long threadId){
        if(this.threads.containsKey(threadId)){
            Thread thread = this.threads.get(threadId);
            if(!thread.isInterrupted()){
                this.threads.get(threadId).interrupt();
            }
        }
    }
}
