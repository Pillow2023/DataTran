package com.pillow.configuration;

/**
 * @author Pillow2023
 * @ClassName ReceiveConfiguration
 * @Description 类的说明
 * @date 2023/3/13
 */

import ch.qos.logback.core.rolling.helper.FileFilterUtil;
import com.pillow.listener.FileListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@EnableConfigurationProperties(ReceiveConfiguration.ReceiveConfigProperties.class)
public class ReceiveConfiguration {

    public ReceiveConfiguration(ReceiveConfigProperties properties){
        if(properties.enable){
            initFileMonitor(properties);
        }
    }

    private void initFileMonitor(ReceiveConfigProperties properties) {
        //监控目录
        String monitorPath = properties.monitorPath;
        //轮询间隔
        Integer intervalTime = properties.intervalTime;
        // 设置文件过滤,只要sql文件
        IOFileFilter filter = FileFilterUtils.or(FileFilterUtils.suffixFileFilter(properties.filterSuffix));
        /// 创建一个文件观察器用于处理文件的格式
        FileAlterationObserver observer = new FileAlterationObserver(monitorPath,filter);
        //添加监听者
        observer.addListener(new FileListener());
        //创建文件变化监听器
        FileAlterationMonitor monitor = new FileAlterationMonitor(intervalTime,observer);
        //开始监控
        try{
            monitor.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Data
    @ConfigurationProperties(prefix = "receive")
    static class ReceiveConfigProperties{
        Boolean enable;
        String monitorPath;
        Integer intervalTime;
        String filterSuffix;
    }
}
