package com.ralsei.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cấu hình CORS cho ứng dụng
 */
@Configuration
/**
 * Configures web for the application.
 */
public class WebConfig {
    /**
     * Cấu hình CORS cho ứng dụng
     * @return WebMvcConfigurer
     */
    @Bean
    /**
     * Executes the cors configurer operation.
     *
     * @return the operation result
     */
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            /**
             * Executes the add cors mappings operation.
             *
             * @param registry the value supplied for this operation
             */
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/api/**") // Áp dụng cho tất cả endpoint /api/
                        .allowedOrigins(
                            "http://localhost:2999", // staff frontend
                            "https://localhost:2999",
                            "https://localhost:3000", // customer frontend 
                            "http://localhost:3000"
                        )
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
