package com.ssccgl.exam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SscCglExamApplication {

    public static void main(String[] args) {
        SpringApplication.run(SscCglExamApplication.class, args);
    }
}
