package com.example.ggkgl.Component;

import com.example.ggkgl.Component.Export.IExport;
import com.example.ggkgl.Component.Import.IImport;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
* 数据导入\导出器工厂
 **/
@Component
public class HandlerFactory implements InitializingBean,ApplicationContextAware {
    private static ApplicationContext applicationContext;

    private Map<String, IExport> exportHandlerImpMap = new HashMap<>();

    private Map<String, IImport> importHandlerImpMap = new HashMap<>();

    @Override
    public void afterPropertiesSet(){
        Map<String, IExport> beanMap1 = applicationContext.getBeansOfType(IExport.class);
        beanMap1.values().forEach((iExport)->
                this.exportHandlerImpMap.put(iExport.handleType().toLowerCase(),iExport));

        Map<String, IImport> beanMap2 = applicationContext.getBeansOfType(IImport.class);
        beanMap2.values().forEach((iImport)->
                this.importHandlerImpMap.put(iImport.handleType().toLowerCase(),iImport));
    }

    public IExport getExportHandler(@NotNull String target) {
        String key = target.toLowerCase();
        if(!this.exportHandlerImpMap.containsKey(key)){
            return null;
        }
        return this.exportHandlerImpMap.get(key);
    }

    public IImport getImportHandler(@NotNull String from) {
        String key = from.toLowerCase();
        if(!this.importHandlerImpMap.containsKey(key)){
            return null;
        }
        return this.importHandlerImpMap.get(key);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        HandlerFactory.applicationContext = applicationContext;
    }
}
