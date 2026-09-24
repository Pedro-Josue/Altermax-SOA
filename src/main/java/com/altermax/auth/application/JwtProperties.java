package com.altermax.auth.application;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("altermax.jwt")
public record JwtProperties(String secret, Duration expiration) {}
