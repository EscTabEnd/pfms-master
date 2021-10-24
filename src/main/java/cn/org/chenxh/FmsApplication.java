package cn.org.chenxh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * @description 主程入口
 * @author chenxh
 * @date 2019-1-21
 */
@SpringBootApplication
public class FmsApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(FmsApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(FmsApplication.class);
    }
}

