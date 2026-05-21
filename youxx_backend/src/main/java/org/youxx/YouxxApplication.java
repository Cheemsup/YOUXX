package org.youxx;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.youxx.mapper")
public class YouxxApplication {

    public static void main(String[] args) {
        SpringApplication.run(YouxxApplication.class, args);
    }

}
