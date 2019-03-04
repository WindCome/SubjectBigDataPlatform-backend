package com.example.ggkgl.AssitClass;

/**
* 进度回调
 **/
public interface ProcessCallBack {
    /**
     * 进度改变
     */
    void onProcessChange(int currentProcess);

    /**
     * 任务log信息
     */
    default void log(String message){}

    /**
     * 任务结束
     */
    default void processFinished(Object result){}

    /**
     * 设置该任务的id
     */
    default void setProgressId(long progressId){}
}
