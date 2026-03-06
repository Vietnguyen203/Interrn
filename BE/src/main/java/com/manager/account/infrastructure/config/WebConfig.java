package com.manager.account.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        String uploadPath = "file:" + System.getProperty("user.dir") + "/" + uploadDir + "/";
        System.out.println("📂 Serving static resources:");
        System.out.println("🛠️ /uploads/** => " + uploadPath);

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}
