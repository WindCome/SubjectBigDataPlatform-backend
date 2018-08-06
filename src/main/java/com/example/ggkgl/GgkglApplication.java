package com.example.ggkgl;

import com.example.ggkgl.Component.MyWebSocket;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class GgkglApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext=SpringApplication.run(GgkglApplication.class, args);
        MyWebSocket.setApplicationContext(applicationContext);//使websocket可以注入其他类。
    }
}
