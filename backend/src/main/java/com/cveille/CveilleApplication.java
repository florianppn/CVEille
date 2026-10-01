package com.cveille;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CveilleApplication {

    public static void main(String[] args) {
        SpringApplication.run(CveilleApplication.class, args);
    }
}
