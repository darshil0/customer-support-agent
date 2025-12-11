package com.example.support;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootConfiguration
@ComponentScan(basePackages = {"com.example.support", "com.google.adk.web"})
public class App {

  public static void main(String[] args) {
    SpringApplication.run(App.class, args);
  }
}
