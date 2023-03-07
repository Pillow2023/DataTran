package com.pillow;

import com.pillow.util.CanalClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author wujt
 * @ClassName CanalApplication
 * @Description 类的说明
 * @date 2023/3/1
 */
@SpringBootApplication
public class CanalApplication implements CommandLineRunner {

    @Autowired
    private CanalClient canalClient;

    public static void main(String[] args){
        SpringApplication.run(CanalApplication.class,args);
    }


    @Override
    public void run(String... args) throws Exception {
        //项目启动，执行canal客户端监听
        canalClient.run();
    }
}
