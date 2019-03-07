package com.example.ggkgl.Component;

import com.example.ggkgl.AssitClass.JSONHelper;
import com.example.ggkgl.AssitClass.ProcessCallBack;
import com.example.ggkgl.Controller.UpdateController;
import com.example.ggkgl.Service.ThreadManagerService;
import javafx.util.Pair;
import net.sf.json.JSONObject;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 用于批量保存所有数据的websocket，为了支持实时返回保存进度而建立。
 */
@ServerEndpoint(value = "/websocket")
@Component
public class MyWebSocket {
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static AtomicInteger onlineCount = new AtomicInteger(0);

    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    private static CopyOnWriteArraySet<MyWebSocket> webSocketSet = new CopyOnWriteArraySet<>();

    private static final ConcurrentHashMap<Long,List<MyWebSocket>> progressListener = new ConcurrentHashMap<>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    private static UpdateController updateController;

    private static ThreadManagerService threadManagerService;

    /**
     * 连接建立成功调用的方法*/
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        webSocketSet.add(this);     //加入set中
        System.out.println("有新连接加入！当前在线人数为" + onlineCount.incrementAndGet());//在线数加1
    }

    public static void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        updateController = applicationContext.getBean(UpdateController.class);
        threadManagerService = applicationContext.getBean(ThreadManagerService.class);
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);  //从set中删除
        removeProgressListener(this);
        System.out.println("有一连接关闭！当前在线人数为" + onlineCount.decrementAndGet());//在线数减1
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息*/
    @OnMessage
    @SuppressWarnings("unchecked")
    public void onMessage(String message, Session session) throws IOException,  EncodeException {
        System.out.println("来自客户端的消息:" + message);
        HashMap params = JSONHelper.jsonStr2Map(message);
        String operateName = params.get("op").toString();
        switch (operateName) {
            case "upgradeSave":
                this.handleSaveRedisData(params);
                break;
            case "cancelJob":
                long jobId = Long.valueOf(params.get("jobId").toString());
                threadManagerService.terminateThread(jobId);
                removeProgressListener(jobId,this);
                break;
            case "startJob":
                jobId = Long.valueOf(params.get("jobId").toString());
                listenProgressRegister(jobId,this);
                threadManagerService.executeThread(jobId);
                break;
        }
    }

    @SuppressWarnings("unchecked")
    private void handleSaveRedisData(HashMap params) throws IOException, EncodeException {
        List<Integer> indexToSave = (List<Integer>)params.getOrDefault("index",null);
        int tableId = (int)params.get("tableId");
        updateController.saveRedisData(tableId, indexToSave, new ProcessCallBack() {
            @Override
            public void onProcessChange(int currentProcess) {
                try {
                    session.getBasicRemote().sendObject(currentProcess);
                }
                catch (IOException | EncodeException e) {
                    e.printStackTrace();
                }
            }
        });
        session.getBasicRemote().sendObject(101);
    }

    /**
     * 注册监听任务
     */
    private static void listenProgressRegister(long progressId,MyWebSocket socket){
        synchronized (progressListener){
            if(!progressListener.containsKey(progressId)){
                progressListener.put(progressId,new CopyOnWriteArrayList<>());
            }
            List<MyWebSocket> listeners = progressListener.get(progressId);
            listeners.add(socket);
        }
    }

    /**
     * 去除某个连接对指定任务的监听
     */
    private static void removeProgressListener(long progressId,MyWebSocket socket){
        if(!progressListener.containsKey(progressId)){
            return;
        }
        synchronized (progressListener){
            List<MyWebSocket> listener = progressListener.get(progressId);
            listener.remove(socket);
            if(listener.size() == 0){
                progressListener.remove(progressId);
            }
        }
    }

    /**
     * 去除某个连接的所有监听
     */
    private static void removeProgressListener(MyWebSocket socket){
        for(Long key :progressListener.keySet()){
            removeProgressListener(key,socket);
        }
    }

     @OnError
     public void onError(Session session, Throwable error) {
         System.out.println("发生错误");
         error.printStackTrace();
     }


     public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
     }

     public void sendMessage(Object message) throws IOException, EncodeException {
        this.session.getBasicRemote().sendObject(message);
     }

    @JmsListener(destination = "progress_object")
    public static void handelProgressMessage(HashMap<String,Object> msg) throws IOException, EncodeException {
        List<MyWebSocket> listeners = progressListener.getOrDefault(msg.get("jobId"),new ArrayList<>(0));
        boolean nobodyListen = true;
        for(MyWebSocket socket:listeners){
            nobodyListen = false;
            socket.sendMessage(JSONObject.fromObject(msg).toString());
        }
        if(nobodyListen){
            System.out.println("无人监听任务"+msg.get("jobId"));
        }
    }


     /**
      * 群发自定义消息
      * */
    public static void sendInfo(String message) throws IOException {
        for (MyWebSocket item : webSocketSet) {
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                continue;
            }
        }
    }

    public static int getOnlineCount() {
        return onlineCount.get();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MyWebSocket && this.session!=null &&
                ((MyWebSocket) obj).session!=null
                &&this.session.getId().equals(((MyWebSocket) obj).session.getId());
    }
}