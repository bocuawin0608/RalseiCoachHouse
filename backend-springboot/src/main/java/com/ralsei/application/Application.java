package com.ralsei.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.ralsei")
@EnableJpaRepositories(basePackages = "com.ralsei.repository")
@EntityScan(basePackages = "com.ralsei.model")
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
@EnableScheduling
/**
 * Provides the application component for the application.
 */
public class Application {
    /**
     * Executes the main operation.
     *
     * @param args the value supplied for this operation
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
