package com.pillow.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.ObjectPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import sun.net.ftp.FtpClient;

import java.io.*;

/**
 * @author Pillow2023
 * @ClassName FtpUtil
 * @Description 连接FTP服务器，实现文件的上传和下载
 * @date 2023/3/1
 */
@Slf4j
@Component
public class FtpUtil {
    /**
     * 连接池初始化标志
     */
    private static volatile boolean hasInit = false;
    /**
     * ftpClient连接池
     */
    private static ObjectPool<FTPClient> ftpClientPool;

    private static String encoding;

    @Value("${ftp.encoding:}")
    public void setEncoding(String encoding){
        FtpUtil.encoding = encoding;
    }

    /**
     * @author: Pillow2023
     * @date: 2023/3/7
     * @Title: init
     * @Description : 初始化
     * @param ftpClientPool
     * @return void
     */
    public static void init(ObjectPool<FTPClient> ftpClientPool){
        if(!hasInit){
            synchronized (FtpUtil.class){
                if(!hasInit){
                    FtpUtil.ftpClientPool = ftpClientPool;
                    hasInit = true;
                }
            }
        }
    }

    /**
     * @author: Pillow2023
     * @date: 2023/3/7
     * @Title: getFTPClient
     * @Description : 获取ftp客户端
     * @return org.apache.commons.net.ftp.FTPClient
     */
    public static FTPClient getFTPClient(){
        checkFtpClientPoolAvailable();
        FTPClient ftpClient = null;
        //异常
        Exception ex = null;
        for(int i = 0;i<3;i++){
            try {
                ftpClient = ftpClientPool.borrowObject();
                //默认切换目录至 /
                ftpClient.changeWorkingDirectory("/");
                //连接成功，则跳出
                break;
            }catch (Exception e){
                ex = e;
            }
        }

        if(ftpClient==null){
            throw new RuntimeException("Cannot obtained ftp client from the connection pool",ex);
        }
        return ftpClient;
    }

    public static void upload(String path, File file){
        FTPClient ftp = getFTPClient();
        try{
            //切换目录
            ftp.changeWorkingDirectory(path);
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            InputStream input = new FileInputStream(file);
            ftp.storeFile(new String(file.getName().getBytes(encoding),"ISO-8859-1"),input);
            input.close();
            log.info("upload file【"+file.getName()+"】 is success!");
        } catch (FileNotFoundException fileNotFoundException){
            fileNotFoundException.printStackTrace();
        } catch (IOException ioException){
            ioException.printStackTrace();
        }finally {
            //释放ftp客户端
            releaseFtpClient(ftp);
        }


    }

    /**
     * @author: Pillow2023
     * @date: 2023/3/8
     * @Title: releaseFtpClient
     * @Description : 释放ftp客户端
     * @param ftpClient
     * @return void
     */
    private static void releaseFtpClient(FTPClient ftpClient) {
        if(ftpClient==null){
            return;
        }

        try {
            //返回对象
            ftpClientPool.returnObject(ftpClient);
        }catch (Exception e){
            log.error("Failed to return the ftp client to the connection pool:{}",e);
            if(ftpClient.isAvailable()){
                try {
                    //断开连接
                    ftpClient.disconnect();
                } catch (IOException ioException) {
                }
            }
        }
        
    }

    /**
     * @author: Pillow2023
     * @date: 2023/3/7
     * @Title: checkFtpClientPoolAvailable
     * @Description : 校验连接池是否启用
     * @param
     * @return void
     */
    private static void checkFtpClientPoolAvailable() {
        Assert.state(hasInit,"FTP未启用或连接失败");
    }
}
