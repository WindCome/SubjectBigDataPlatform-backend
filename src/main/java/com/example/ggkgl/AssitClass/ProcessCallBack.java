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
     * 进度log信息
     */
    default void log(String message){}

    /**
     * 强制进度结束
     */
    default void processFinished(){}
}
