package com.ralsei.driver.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.ralsei.driver.feign")
@ComponentScan(basePackages = "com.ralsei.driver")
@EntityScan(basePackages = "com.ralsei.driver.model")
@EnableJpaRepositories(basePackages = "com.ralsei.driver.repository")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
