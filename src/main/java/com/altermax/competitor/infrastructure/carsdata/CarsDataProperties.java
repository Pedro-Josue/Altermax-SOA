package com.altermax.competitor.infrastructure.carsdata;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("altermax.cars-data")
public record CarsDataProperties(
        String baseUrl, String apiKey, Duration connectTimeout, Duration readTimeout) {}
