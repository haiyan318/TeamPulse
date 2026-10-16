package com.example.teampulse;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * TeamPulse 启动类。
 */
@SpringBootApplication
@MapperScan("com.example.teampulse.mapper")
public class TeamPulseApplication {

    public static void main(String[] args) {
        SpringApplication.run(TeamPulseApplication.class, args);
    }
}
