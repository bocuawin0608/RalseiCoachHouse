package com.ralsei.staff.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.ralsei.staff")
@EnableFeignClients(basePackages = "com.ralsei.staff.feign")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
