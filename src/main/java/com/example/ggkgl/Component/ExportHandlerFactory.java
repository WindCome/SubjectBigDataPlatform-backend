package com.example.ggkgl.Component;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
* 数据导出器工厂
 **/
@Component
public class ExportHandlerFactory implements InitializingBean,ApplicationContextAware {
    private static ApplicationContext applicationContext;

    private Map<String, IExport> handlerImpMap = new HashMap<>();

    @Override
    public void afterPropertiesSet(){
        Map<String, IExport> beanMap = applicationContext.getBeansOfType(IExport.class);
        beanMap.values().forEach((iExport)->
                this.handlerImpMap.put(iExport.handleType().toLowerCase(),iExport));
    }

    public IExport getExportHandler(@NotNull String target) {
        String key = target.toLowerCase();
        if(!this.handlerImpMap.containsKey(key)){
            return null;
        }
        return this.handlerImpMap.get(key);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ExportHandlerFactory.applicationContext = applicationContext;
    }
}
