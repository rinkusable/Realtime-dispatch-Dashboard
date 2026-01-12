package com.DispatchScreen.LiveDispatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class LiveDispatchApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(LiveDispatchApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(LiveDispatchApplication.class, args);
    }
}

