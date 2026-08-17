package com.streamsocial.eventtaxonomy.config;

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
                registry.addMapping("/api/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
                registry.addMapping("/cluster/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "OPTIONS")
                        .allowedHeaders("*");
                registry.addMapping("/consumers/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "OPTIONS")
                        .allowedHeaders("*");
                registry.addMapping("/events/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}
