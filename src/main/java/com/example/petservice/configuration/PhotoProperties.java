package com.example.petservice.configuration;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "photo")
public class PhotoProperties {
    private Integer maxFileSize;
    private Integer connectionTimeout;
    private Integer readTimeout;
}
