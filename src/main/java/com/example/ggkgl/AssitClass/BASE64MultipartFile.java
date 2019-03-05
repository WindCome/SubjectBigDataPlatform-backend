package com.example.ggkgl.AssitClass;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;

public class BASE64MultipartFile implements MultipartFile {
    private String header;
    private byte[] content;
    private long size;//字节大小
    private InputStream inputStream;//输入流
    private String type;//文件类型
    private String name;
    private String originalName;

    public String getHeader() {
        return header;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOriginalFilename() {
        return originalName;
    }

    @Override
    public String getContentType() {
        return type;
    }

    @Override
    public boolean isEmpty() {
        return size==0;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return content;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return inputStream;
    }

    @Override
    public void transferTo(File file) throws IOException, IllegalStateException {
        new FileOutputStream(file).write(content);
    }

    BASE64MultipartFile(String header, byte[] content) {
        this.header = header.split(";")[0];
        this.content = content;
        this.size=content==null?0:content.length;
        this.inputStream=new ByteArrayInputStream(content);
        this.type=this.header.split(":")[1];
        this.name=String.valueOf(System.currentTimeMillis())+(int)(Math.random()*10000)
                +"."+transferSuffix(this.header.split("/")[1]);
        this.originalName=this.name;
    }

    private String transferSuffix(String suffix){
        switch (suffix){
            case "vnd.openxmlformats-officedocument.wordprocessingml.document":
                return "docx";
            case "plain":
                return "txt";
            case "msword":
                return "doc";
            default:
                return suffix;//jped,pdf,png
        }
    }
}
