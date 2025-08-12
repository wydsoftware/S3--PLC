package com.pda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 移动端派工系统主应用类
 * 
 * @author PDA System
 * @version 1.0.0
 * @since 2025-01-08
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableAsync
@EnableScheduling
public class PdaSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(PdaSystemApplication.class, args);
        System.out.println("\n" +
            "=================================================\n" +
            "  移动端派工系统启动成功!\n" +
            "  访问地址: http://localhost:8080\n" +
            "  API文档: http://localhost:8080/swagger-ui.html\n" +
            "  健康检查: http://localhost:8080/actuator/health\n" +
            "=================================================\n");
    }
}