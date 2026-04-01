package com.ncg;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot 启动类
 */
@SpringBootApplication
@MapperScan("com.ncg.dal.mapper")
@EnableScheduling
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        System.out.println("===========================================");
        System.out.println("食品安全溯源系统 - Core Service 启动成功！");
        System.out.println("访问地址：http://localhost:8081");
        System.out.println("登录接口：POST /api/auth/login");
        System.out.println("===========================================");
    }
}