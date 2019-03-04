package com.example.ggkgl;

import com.example.ggkgl.Component.MyWebSocket;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.URL;

@SpringBootApplication
@EnableCaching
public class GgkglApplication {
    //log4j配置
    static
    {
        try
        {
            URL resourcePath=GgkglApplication.class.getClassLoader().getResource("");
            if(resourcePath == null)
                throw new NullPointerException();
            String log4jPath=resourcePath.getPath()+"/log4j.properties";
            PropertyConfigurator.configure(log4jPath);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext=SpringApplication.run(GgkglApplication.class, args);
        MyWebSocket.setApplicationContext(applicationContext);//使websocket可以注入其他类。
    }
}
