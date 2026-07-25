package com.neusoft.amos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * AMOS 主应用入口（模块化单体）。
 * 各业务域以 package 边界组织（register / maintenance / stock），
 * 后续可整体抽离为独立微服务，无需改动业务代码。
 */
@SpringBootApplication(scanBasePackages = "com.neusoft.amos")
@EntityScan(basePackages = "com.neusoft.amos")
@EnableJpaRepositories(basePackages = "com.neusoft.amos")
public class AmosServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AmosServerApplication.class, args);
    }
}
