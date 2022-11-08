package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/common")
public class CommonController {
    @Value("${reggie.path}")
    private String basePath;

    /**
     * 图片上传
     * @param file
     * @return
     */
    @PostMapping ("/upload")
    public R<String> upload(MultipartFile file){
        //得到原文件名
        String originalFilename = file.getOriginalFilename();
        //得到后缀
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        //判断是否有该目录
        File dir = new File(basePath);
        if(!dir.exists()){
            dir.mkdir();
        }
        //使用uuid生成文件名
        String fileName = UUID.randomUUID() + suffix;
        try {
            file.transferTo(new File(basePath + fileName));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return R.success(fileName);
    }

    /**
     * 回显图片
     * @param response
     * @param name
     */
    @GetMapping("/download")
    public void download(HttpServletResponse response,String name) {
        try {
            //创建输入流
            FileInputStream fis = new FileInputStream(new File(basePath + name));
            //创建输出流
            ServletOutputStream os = response.getOutputStream();
            //设置写回去的类型
            response.setContentType("image/jpeg");
            //读写流
            int len =0;  //读取的长度
            byte[] bytes = new byte[1024];  //缓存区
            while ((len=fis.read(bytes)) != -1){  //读取缓存区里的数据，-1为最后一个字节
                os.write(bytes,0,len);  //边读边写，从0开始到当前读取长度结束
            }
            //关闭流
            os.close();
            fis.close();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
