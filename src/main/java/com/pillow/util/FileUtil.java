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
     * @param dirpath     目录路径
     * @param startFormat 起始是否格式化，为true，则目录骑士不为"/"，则添加"/"，
     * @return java.lang.String
     * @author: wujt
     * @date: 2022/7/14
     * @Title: dirpathFormat
     * @Description : 目录路径格式化，当目录路径结尾不为"/"，则添加"/"，
     */
    public static String dirpathFormat(String dirpath, boolean startFormat) {
        if (!dirpath.endsWith("/")) {
            dirpath += "/";
        }
        if (startFormat) {
            dirpath = dirpath.startsWith("/") ? dirpath : "/" + dirpath;
        }
        mkdirs(dirpath);
        return dirpath;
    }

    /**
     * @author: wujt
     * @date: 2023/3/8
     * @Title: dirPathSplice
     * @Description : 目录路径拼接
     * @param dirPath1
     * @param dirPath2
     * @return java.lang.String
     */
    public static String dirPathSplice(String dirPath1,String dirPath2){
        String dirPath = "";
        if(dirPath1.endsWith("/")&&dirPath2.startsWith("/")){
            dirPath = dirPath1.substring(0,dirPath1.length()-1)+dirPath2;
        }else if(dirPath1.endsWith("/")||dirPath2.startsWith("/")){
            dirPath = dirPath1+dirPath2;
        }
        mkdirs(dirPath);
        return dirPath;
    }

    /**
     * @param destPath
     * @author: wujt
     * @date: 2022年6月7日
     * @Title: mkdirs
     * @Description: 创建文件夹
     */
    public static void mkdirs(String destPath) {
        File file = new File(destPath);
        // 当文件夹不存在时，mkdirs会自动创建多层目录，区别于mkdir．(mkdir如果父目录不存在则会抛出异常)
        if (!file.exists() && !file.isDirectory()) {
            file.mkdirs();
        }
    }
}

