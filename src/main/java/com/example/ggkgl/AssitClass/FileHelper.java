package com.example.ggkgl.AssitClass;

import org.apache.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

/**
* 文件辅助类
 **/
public class FileHelper {
    private static final Logger logger=Logger.getLogger(FileHelper.class);
    /**
     * @param base base64字符串
     * @return MutipartFile类型
     */
    public static MultipartFile Base64ToMultipartFile(String base) throws Exception {
        try{
            Base64.Decoder decoder=Base64.getDecoder();
            byte[] bytes=new byte[0];
            String[] str=base.split(",");
            if(str.length!=2){
                return null;
            }
            bytes= decoder.decode(str[1]);
            return new BASE64MultipartFile(str[0],bytes);
        }
        catch(Exception e){
            logger.warn(e.getMessage());
            throw new Exception("文件转换错误");
        }
    }
}
