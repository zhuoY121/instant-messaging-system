package com.zhuo.message;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication()
@MapperScan("com.zhuo.message.dao.mapper")
public class MessageStorageApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessageStorageApplication.class, args);
    }

}
