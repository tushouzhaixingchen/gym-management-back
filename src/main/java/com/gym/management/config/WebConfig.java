package com.gym.management.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置跨域请求 (CORS)
     * 注意：Spring Security 中也配置了 CORS，这里作为 MVC 层面的补充，确保静态资源和非安全链请求也能跨域
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 允许所有路径
                .allowedOrigins("http://localhost:5173", "http://localhost:3000", "http://127.0.0.1:5173", "http://127.0.0.1:3000") // 允许的源 (开发环境)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH") // 允许的方法
                .allowedHeaders("*") // 允许的头部
                .allowCredentials(true) // 允许携带凭证 (Cookie/Token)
                .maxAge(3600); // 预检请求缓存时间
    }

    /**
     * 配置静态资源映射
     * 如果前端页面放在 resources/static 或 resources/public 下，可通过此配置访问
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射 /static/** 到 classpath:/static/
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");

        // 映射 /public/** 到 classpath:/public/
        registry.addResourceHandler("/public/**")
                .addResourceLocations("classpath:/public/");
    }


}