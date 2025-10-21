package com.fpt.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Áp dụng cho tất cả các endpoint
                        .allowedOrigins("*") // Cho phép tất cả domain, hoặc thay bằng frontend của bạn
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        .allowedHeaders("*") // Cho phép tất cả header
                        .exposedHeaders("Authorization", "Content-Disposition") // nếu bạn muốn client đọc được các header này
                        .allowCredentials(false) // Nếu bạn dùng cookie hoặc Authorization header thì để true
                        .maxAge(3600); // cache pre-flight request 1h
            }
        };
    }
}
