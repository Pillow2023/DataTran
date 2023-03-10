package com.pillow.configuration;


import com.pillow.util.FtpUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;

@Slf4j
@Configuration
@ConditionalOnClass({GenericObjectPool.class, FTPClient.class})
@EnableConfigurationProperties(FTPConfiguration.FtpConfigProperties.class)
public class FTPConfiguration {

    /**
     * 连接池
     */
    private ObjectPool<FTPClient> pool;

    public FTPConfiguration(FtpConfigProperties props){
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTimeMillis(60000);
        poolConfig.setSoftMinEvictableIdleTimeMillis(50000);
        poolConfig.setTimeBetweenEvictionRunsMillis(30000);
        pool = new GenericObjectPool<>(new FtpClientPooledObjectFactory(props), poolConfig);
        //预加载FtpClient连接到对象中
        preLoadingFtpClient(props.getInitialSize(),poolConfig.getMaxIdle());
        //初始化Ftp工具类的ftpClient连接池
        FtpUtil.init(pool);
    }

    /**
     * @author: Pillow2023
     * @date: 2023/3/2
     * @Title: preLoadingFtpClient
     * @Description : 预加载FtpClient连接到对象中
     * @param initalSize	初始化连接数
     * @param maxIdleSize	最大空闲连接数
     * @return void
     */
    private void preLoadingFtpClient(Integer initalSize,int maxIdleSize){
        if(initalSize==null||initalSize<0){
            return;
        }
        int size = Math.min(initalSize.intValue(),maxIdleSize);
        for(int i = 0;i<size;i++){
            try {
                pool.addObject();
            } catch (Exception e) {
                log.error("Failed to preload the ftp connection object:{}",e);
            }
        }
    }

    /**
     * @author: Pillow2023
     * @date: 2023/3/2
     * @Title: destroy
     * @Description : 销毁连接池
     * @param
     * @return void
     */
    @PreDestroy
    public void destroy(){
        if(pool!=null){
            pool.close();
            log.info("Destroy ftpClientPool...");
        }
    }


    /**
     * Ftp配置属性类，建立ftpClient时使用
     */
    @Data
    @ConfigurationProperties(prefix = "ftp")
    static class FtpConfigProperties {

        private String host;

        private int port;

        private String username;

        private String password;

        private int bufferSize = 8096;

        private String encoding;

        /**
         * 初始化连接数
         */
        private Integer initialSize = 0;

    }



    /**
     * FtpClient对象工厂类
     */
    static class FtpClientPooledObjectFactory implements PooledObjectFactory<FTPClient>{

        private FtpConfigProperties props;

        public FtpClientPooledObjectFactory(FtpConfigProperties props){
            this.props = props;
        }

        /**
         * @author: Pillow2023
         * @date: 2023/3/2
         * @Title: makeObject
         * @Description : 创建连接池对象
         * @param
         * @return org.apache.commons.pool2.PooledObject<org.apache.commons.net.ftp.FTPClient>
         */
        @Override
        public PooledObject<FTPClient> makeObject() throws Exception {
            FTPClient ftpClient = new FTPClient();
            try {
                ftpClient.connect(props.getHost(), props.getPort());
                ftpClient.login(props.getUsername(), props.getPassword());
                log.debug("Connect FTP server return code:{}", ftpClient.getReplyCode());
                ftpClient.setBufferSize(props.getBufferSize());
                ftpClient.setControlEncoding(props.getEncoding());
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                // ftpClient.enterLocalPassiveMode();
                return new DefaultPooledObject<>(ftpClient);
            }catch (Exception e){
                log.error("Failed to set up the  connection to Ftp:{}",e);
                if(ftpClient.isAvailable()){
                    ftpClient.disconnect();
                }
                throw new Exception("Failed to set up the  connection to Ftp", e);
            }
        }

        /**
         * @author: Pillow2023
         * @date: 2023/3/2
         * @Title: destroyObject
         * @Description : 销毁对象，关闭连接
         * @param p	
         * @return void
         */
        @Override
        public void destroyObject(PooledObject<FTPClient> p) throws Exception {
            FTPClient ftpClient = getObject(p);
            if(ftpClient!=null&&ftpClient.isConnected()){
                ftpClient.disconnect();
            }
        }

        /**
         * @author: Pillow2023
         * @date: 2023/3/2
         * @Title: validateObject
         * @Description : 校验对象
         * @param p
         * @return boolean
         */
        @Override
        public boolean validateObject(PooledObject<FTPClient> p) {
            FTPClient ftpClient = getObject(p);
            if(ftpClient==null||!ftpClient.isConnected()){
                return false;
            }
            try {
                ftpClient.changeWorkingDirectory("/");
                return true;
            }catch (Exception e) {
                log.error("Failed to verify the FTP connection::{}", e);
                return false;
            }
        }

        @Override
        public void activateObject(PooledObject<FTPClient> p) throws Exception {

        }

        @Override
        public void passivateObject(PooledObject<FTPClient> p) throws Exception {

        }

        /**
         * @author: Pillow2023
         * @date: 2023/3/2
         * @Title: getObject
         * @Description : 获取连接池对象
         * @param p
         * @return org.apache.commons.net.ftp.FTPClient
         */
        private FTPClient getObject(PooledObject<FTPClient> p){
            if(p==null||p.getObject()==null){
                return null;
            }
            return p.getObject();
        }
    }

}
