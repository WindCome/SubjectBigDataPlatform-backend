package com.example.ggkgl.AssitClass;

import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
* JSON工具类
 **/
public class JSONHelper {

    /**
    *  JSON转HashMap
     */
    public static HashMap<String,Object> json2Map(JSONObject jsonObject){
        Iterator iterator = jsonObject.keys();
        HashMap<String, Object> result = new HashMap<>();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            result.put(key, jsonObject.get(key));
        }
        return result;
    }

    /**
    * map转json
     */
    public static String map2Json(Map map){
        if(map == null){
            return null;
        }
        return JSONObject.fromObject(map).toString();
    }

    /**
    * 根据json字符串构造map
     */
    public static HashMap<String,Object> jsonStr2Map(String jsonStr){
        JSONObject jsonObject = JSONObject.fromObject(jsonStr);
        return JSONHelper.json2Map(jsonObject);
    }
}
