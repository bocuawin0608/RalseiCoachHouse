package com.ralsei.customer.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.ralsei.customer.feign")
@ComponentScan(basePackages = "com.ralsei.customer")
@EntityScan(basePackages = "com.ralsei.customer.model")
@EnableJpaRepositories(basePackages = "com.ralsei.customer.repository")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
