package com.pda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * PDA移动端派工系统主应用类
 * 
 * @author PDA System
 * @version 1.0.0
 * @since 2024-01-08
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class PdaApplication {

    public static void main(String[] args) {
        SpringApplication.run(PdaApplication.class, args);
        System.out.println("\n" +
                "=================================================\n" +
                "  PDA移动端派工系统启动成功！\n" +
                "  系统版本: v1.0.0\n" +
                "  访问地址: http://localhost:8080/api\n" +
                "  API文档: http://localhost:8080/api/swagger-ui.html\n" +
                "  健康检查: http://localhost:8080/api/actuator/health\n" +
                "=================================================\n");
    }
}