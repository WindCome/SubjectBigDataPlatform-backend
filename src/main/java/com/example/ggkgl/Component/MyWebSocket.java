package com.example.ggkgl.Component;

import com.example.ggkgl.AssitClass.JSONHelper;
import com.example.ggkgl.AssitClass.ProcessCallBack;
import com.example.ggkgl.Controller.UpdateController;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 用于批量保存所有数据的websocket，为了支持实时返回保存进度而建立。
 */
@ServerEndpoint(value = "/websocket/{tableId}")
@Component
public class MyWebSocket {
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static AtomicInteger onlineCount = new AtomicInteger(0);

    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    private static CopyOnWriteArraySet<MyWebSocket> webSocketSet = new CopyOnWriteArraySet<>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    private int tableId;

    private static UpdateController updateController;

    /**
     * 连接建立成功调用的方法*/
    @OnOpen
    public void onOpen(Session session,@PathParam("tableId")int tableId) {
        this.session = session;
        this.tableId=tableId;
        webSocketSet.add(this);     //加入set中
        System.out.println("有新连接加入！当前在线人数为" + onlineCount.incrementAndGet());//在线数加1
    }

    public static void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        updateController = applicationContext.getBean(UpdateController.class);
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);  //从set中删除
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
        if(operateName.equals("upgradeSave")){
            List<Integer> indexToSave = (List<Integer>)params.get("index");
            System.out.println("保存的下标为:");
            indexToSave.forEach(System.out::println);
            updateController.saveRedisData(this.tableId, indexToSave, new ProcessCallBack() {
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
    }

     @OnError
     public void onError(Session session, Throwable error) {
         System.out.println("发生错误");
         error.printStackTrace();
     }


     public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
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
}