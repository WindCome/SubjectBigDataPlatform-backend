package com.example.ggkgl.Component;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
* 数据导出器工厂
 **/
@Component
public class ExportHandlerFactory implements InitializingBean{
    private Map<String, IExport> handlerImpMap = new HashMap<>();

    @Override
    public void afterPropertiesSet(){
        Map<String, IExport> beanMap = SpringUtil.getBeansOfType(IExport.class);
        beanMap.values().forEach((iExport)->{
            this.handlerImpMap.put(iExport.handleType().toLowerCase(),iExport);
        });
    }

    public IExport getExportHandler(@NotNull String target) {
        String key = target.toLowerCase();
        if(!this.handlerImpMap.containsKey(key)){
            return null;
        }
        return this.handlerImpMap.get(key);
    }
}
