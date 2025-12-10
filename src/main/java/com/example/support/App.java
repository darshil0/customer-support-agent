package com.example.support;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.example.support", "com.google.adk.web"})
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
