package com.pillow.listener;

import com.pillow.util.DBUtil;
import com.pillow.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;

import java.io.*;

/**
 * @author Pillow2023
 * @ClassName FileListener
 * @Description 类的说明
 * @date 2023/3/13
 */
@Slf4j
public class FileListener extends FileAlterationListenerAdaptor {

    @Override
    public void onFileCreate(File file) {
        String sql = FileUtil.getFileContent(file);
        log.info("Create file:{}",file.getAbsoluteFile());
        DBUtil.executeSql(sql);

    }


}
