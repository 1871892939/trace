package com.ncg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Spring Boot 启动类（网关服务）
 */
@SpringBootApplication
@EnableDiscoveryClient
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        System.out.println("===========================================");
        System.out.println("食品安全溯源系统 - Gateway Service 启动成功！");
        System.out.println("访问地址：http://localhost:8080");
        System.out.println("===========================================");
    }
}