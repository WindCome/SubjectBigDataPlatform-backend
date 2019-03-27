package com.example.ggkgl.Service;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.OperationNotSupportedException;
import javax.validation.constraints.NotNull;
import java.io.*;
import java.nio.charset.Charset;
import java.util.List;

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
    public ResponseEntity<FileSystemResource> download(@NotNull String fileName) throws UnsupportedEncodingException,
            OperationNotSupportedException {
        fileName = STORAGE_FILE_PATH + File.separator +fileName;
        File file = new File(fileName);
        return this.download(file);
    }

    /**
     * @param file 文件
     * @return 下载文件实体
     */
    public ResponseEntity<FileSystemResource> download(@NotNull File file) throws UnsupportedEncodingException,
            OperationNotSupportedException {
        if(!file.exists()){
            throw new OperationNotSupportedException("不存在该资源: "+file.getAbsolutePath());
        }
        File rootDir = new File(STORAGE_FILE_PATH);
        if (file.getParentFile().equals(rootDir)) {
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
        }else{
            throw new OperationNotSupportedException("无权访问该文件");
        }
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

    /**
     * 创建一个临时文件
     * @param postFix 文件后缀
     */
    public File createTmpFile(String postFix) throws IOException {
        boolean created = false;
        File file=null;
        while(!created){
            String storagePath = STORAGE_FILE_PATH+File.separator+System.currentTimeMillis()+
                    RandomStringUtils.randomAlphanumeric(4)+"."+postFix;
            file = new File(storagePath);
            created = file.createNewFile();
        }
        return file;
    }

    /**
     * @param content 文本内容
     * @param fileName 文件名称
     * @throws IOException 生成文件夹出错、保存文件出错
     */
    public void saveFile(List<String> content,String fileName) throws IOException
    {
        String path = ResourceService.STORAGE_FILE_PATH;
        if(StringUtils.isEmpty(fileName)){
            fileName = System.currentTimeMillis()+".txt";
        }
        File fileDir = new File(path);
        if (!fileDir.exists()&&!fileDir.mkdirs())
            throw new IOException();
        String absolutePath= path+File.separator+fileName;
        File file = new File(absolutePath);
        try(FileOutputStream fileOutputStream = new FileOutputStream(file)){
            content.forEach(str->{
                try {
                    final String tmp = str+"\r\n";
                    fileOutputStream.write(tmp.getBytes(Charset.forName("utf-8")));
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
