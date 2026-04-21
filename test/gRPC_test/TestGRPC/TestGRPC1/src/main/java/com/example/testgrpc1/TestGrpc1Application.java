package com.example.testgrpc1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class TestGrpc1Application {

    public static void main(String[] args) {
        SpringApplication.run(TestGrpc1Application.class, args);
    }

}
