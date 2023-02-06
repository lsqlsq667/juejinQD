package com.juejin.qd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class JuejinQdApplication {

    public static void main(String[] args) {
        SpringApplication.run(JuejinQdApplication.class, args);
    }

}
