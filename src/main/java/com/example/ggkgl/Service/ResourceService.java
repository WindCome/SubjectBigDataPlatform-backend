package com.example.ggkgl.Service;

import org.apache.log4j.Logger;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.OperationNotSupportedException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
* 文件下载、上传
 **/
@Service
public class ResourceService {
    public final static String STORAGE_FILE_PATH = "D:\\Project\\GraduationProject\\GGKWH\\temp";

    private Logger logger = Logger.getLogger(Resource.class);

    public ResourceService() throws IOException {
        File file = new File(STORAGE_FILE_PATH);
        if (!file.exists()&&!file.mkdirs())
            throw new IOException();
    }

    /**
     * @param fileName 资源的url
     * @return 下载文件实体
     */
    public ResponseEntity<FileSystemResource> download(String fileName) throws UnsupportedEncodingException,
            OperationNotSupportedException {
        Assert.notNull(fileName,"下载url不能为空");
        fileName = STORAGE_FILE_PATH + File.separator +fileName;
        File file = new File(fileName);
        if (file.exists()) {
            HttpHeaders headers = new HttpHeaders();
            String contentDisposition = new String(file.getName().getBytes(),"utf-8");
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Content-Disposition", String.format("attachment;filename=\"%s\"",contentDisposition) );
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentLength(file.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new FileSystemResource(file));
        }
        throw new OperationNotSupportedException("不存在该资源: "+fileName);
    }

    /**
     * @param multipartFile 文件实体
     * @return  文件储存的绝对路径
     */
    public String uploadFile(MultipartFile multipartFile) throws IOException {

        if(multipartFile == null || multipartFile.isEmpty() || multipartFile.getOriginalFilename().isEmpty())
            throw new IOException("上传文件为空");

        String contentType = multipartFile.getContentType();
        if (!contentType.contains(""))
            throw new IOException("");

        String filePath = STORAGE_FILE_PATH;
        String absolutePath;
        try
        {
            absolutePath = this.saveFile(multipartFile, filePath);
            this.logger.info("上传文件: "+absolutePath+" (fileName); "+filePath+" (filePath);");
            return multipartFile.getOriginalFilename();
        }
        catch (IOException t)
        {
            t.printStackTrace();
            throw t;
        }
    }

    /**
     * @param multipartFile 要保存的文件
     * @param path 存储目的目录
     * @return  文件绝对路径
     * @throws IOException 生成文件夹出错、保存文件出错
     */
    private String saveFile(MultipartFile multipartFile, String path) throws IOException
    {
        File file = new File(path);
        if (!file.exists()&&!file.mkdirs())
            throw new IOException();
        String absolutePath= path+File.separator+multipartFile.getOriginalFilename();
        File saveFile=new File(absolutePath);
        multipartFile.transferTo(saveFile);
        this.logger.info("保存文件: "+absolutePath+" (absolutePath); "
                +saveFile.getAbsolutePath()+" (saveFile.getAbsolutePath());");
        return saveFile.getAbsolutePath();
    }
}
