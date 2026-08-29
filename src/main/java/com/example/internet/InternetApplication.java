package com.example.internet;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.internet.mapper")
public class InternetApplication {

    public static void main(String[] args) {
        SpringApplication.run(InternetApplication.class, args);
    }
}
