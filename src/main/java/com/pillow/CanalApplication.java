package com.pillow;

import com.pillow.client.CanalClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Pillow2023
 * @ClassName CanalApplication
 * @Description 类的说明
 * @date 2023/3/1
 */
@SpringBootApplication
public class CanalApplication implements CommandLineRunner {

    @Autowired
    private CanalClient canalClient;

    @Value("${send.enable:}")
    private Boolean sendEnable;

    public static void main(String[] args){
        SpringApplication.run(CanalApplication.class,args);
    }


    @Override
    public void run(String... args) throws Exception {
        if(sendEnable){
            canalClient.run();
        }
    }
}
