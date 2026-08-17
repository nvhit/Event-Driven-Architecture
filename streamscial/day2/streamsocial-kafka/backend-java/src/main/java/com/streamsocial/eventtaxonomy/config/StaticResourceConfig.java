package com.streamsocial.eventtaxonomy.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve frontend dist files from the frontend/dist directory
        registry.addResourceHandler("/**")
                .addResourceLocations("file:../frontend/dist/")
                .resourceChain(true);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Forward root to index.html for SPA routing
        registry.addViewController("/").setViewName("forward:/index.html");
    }
}
