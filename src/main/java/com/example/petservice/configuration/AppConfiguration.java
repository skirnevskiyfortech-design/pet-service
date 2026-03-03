package com.example.petservice.configuration;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ComponentScan(basePackages = {"com.example.petservice", "org.openapi.example.*"})
public class AppConfiguration implements WebMvcConfigurer {
}
