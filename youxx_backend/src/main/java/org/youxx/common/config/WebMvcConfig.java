package org.youxx.common.config;

import lombok.extern.slf4j.Slf4j;
import org.youxx.common.interceptor.JwtTokenUserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    // 静态资源根目录：通过配置注入，避免依赖启动时的工作目录(user.dir)
    // dev 环境在 application-dev.yaml 显式写死仓库根路径
    @Value("${youxx.resource.base-dir}")
    private String resourceBaseDir;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/**",
                        "/api/product/list",
                        "/api/product/hot",
                        "/api/product/category/list",
                        "/api/product/**/detail"
                );
        log.info("自定义拦截器注册完成");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 所有图片(内置商品图 + 运行时上传的头像/商品图)统一落盘到 upload_resources/，
        // 对外通过 /upload_resources/** 静态服务提供，路径与物理目录一致、无歧义
        String baseDir = resourceBaseDir;
        log.info("静态资源根目录: baseDir={}", baseDir);
        registry.addResourceHandler("/upload_resources/**")
                .addResourceLocations("file:" + baseDir + "/upload_resources/");
    }
}
