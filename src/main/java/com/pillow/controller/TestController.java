package com.pillow.controller;

import com.pillow.util.FileUtil;
import com.pillow.util.FtpUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

/**
 * @author Pillow2023
 * @ClassName TestController
 * @Description 类的说明
 * @date 2023/3/7
 */
@RestController
@RequestMapping("test")
public class TestController {

    @RequestMapping("getFtp")
    public void getFtp(){
        String ftpPath = "/opt/canal_data/";
        File file = new File("D:/canal/send/2023-03-02 15-01-19-591.sql");
        FtpUtil.upload(ftpPath,file);
    }
}
