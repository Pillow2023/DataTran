package com.pillow.util;

import java.io.File;

/**
 * @author wujt
 * @ClassName FileUtil
 * @Description 文件工具类
 * @date 2023/3/2
 */
public class FileUtil {

    /**
     * @author: wujt
     * @date: 2023/3/2
     * @Title: getFormatDir
     * @Description : 获取格式化目录路径，结尾不为"/"
     * @param dirPath
     * @return java.lang.String
     */
    public static String getFormatDir(String dirPath){
        dirPath.replace("\\","/");
        //目录不存时，创建目录
        File dir = new File(dirPath);
        if(!dir.exists()){
            dir.mkdir();
        }
        //格式化目录
        if(dirPath.lastIndexOf("/")==(dirPath.length()-1)){
            dirPath = dirPath.substring(0,dirPath.length()-1);
        }
        return dirPath;
    }
}
