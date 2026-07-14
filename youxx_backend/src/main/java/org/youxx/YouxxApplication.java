package org.youxx;

import org.springframework.cache.annotation.EnableCaching;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.youxx.mapper")
@EnableCaching
public class YouxxApplication {

    public static void main(String[] args) {
        SpringApplication.run(YouxxApplication.class, args);
    }

}
