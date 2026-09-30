package com.ralsei.auth.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.ralsei.auth.feign")
@ComponentScan(basePackages = "com.ralsei.auth")
@EntityScan(basePackages = "com.ralsei.auth.model")
@EnableJpaRepositories(basePackages = "com.ralsei.auth.repository")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
