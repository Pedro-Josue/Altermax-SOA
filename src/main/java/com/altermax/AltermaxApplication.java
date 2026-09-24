package com.altermax;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AltermaxApplication {
    public static void main(String[] args) {
        SpringApplication.run(AltermaxApplication.class, args);
    }
}
