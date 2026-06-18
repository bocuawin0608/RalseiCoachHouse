package com.ralsei.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication(scanBasePackages = "com.ralsei")
@EnableJpaRepositories(basePackages = "com.ralsei.repository")
@EntityScan(basePackages = "com.ralsei.model")
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}