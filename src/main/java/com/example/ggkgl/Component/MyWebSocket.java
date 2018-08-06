package com.example.ggkgl.Component;

import com.example.ggkgl.Controller.UpdateController;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;


/**
 * 用于批量保存所有数据的websocket，为了支持实时返回保存进度而建立。
 */
@ServerEndpoint(value = "/websocket/{tableId}")
@Component
public class MyWebSocket {
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;

    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    private static CopyOnWriteArraySet<MyWebSocket> webSocketSet = new CopyOnWriteArraySet<MyWebSocket>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    private int tableId;
    private static ApplicationContext applicationContext;
    private UpdateController updateController;
    private String status="false";


    /**
     * 连接建立成功调用的方法*/
    @OnOpen
    public void onOpen(Session session,@PathParam("tableId")int tableId) {
        this.session = session;
        this.tableId=tableId;
        webSocketSet.add(this);     //加入set中
        addOnlineCount();           //在线数加1
        System.out.println("有新连接加入！当前在线人数为" + getOnlineCount());
    }

    public static void setApplicationContext(ApplicationContext context)
    {
        applicationContext=context;
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);  //从set中删除
        subOnlineCount();           //在线数减1
        System.out.println("有一连接关闭！当前在线人数为" + getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息*/
    @OnMessage
    public void onMessage(String message, Session session) throws IOException,  EncodeException {
        System.out.println("来自客户端的消息:" + message);
        updateController=(UpdateController)applicationContext.getBean(UpdateController.class);
        Jedis jedis=new Jedis();
        String jsonStr=jedis.get("upgrade"+tableId);
        JSONArray jsonArray=JSONArray.fromObject(jsonStr);
        System.out.println("!!");
        for(int i=0;i<jsonArray.size();i++) {
            int NO=i+1;
            String status=jsonArray.getJSONObject(i).getString("status");
            if(status.equals("update")||status.equals("new")) {
                if (!updateController.upgradeSave(i, tableId)) {
                    session.getBasicRemote().sendObject(-1);
                    System.out.println(NO);
                    return ;
                }
            }
            System.out.println(NO);
            float process=(float)NO/jsonArray.size();
            int percent=(int)Math.rint(process*100);
            session.getBasicRemote().sendObject(percent);
        }
        session.getBasicRemote().sendObject(101);
    }



     @OnError
     public void onError(Session session, Throwable error) {
     System.out.println("发生错误");
     error.printStackTrace();
     }


     public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
     //this.session.getAsyncRemote().sendText(message);
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

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        MyWebSocket.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        MyWebSocket.onlineCount--;
    }
}