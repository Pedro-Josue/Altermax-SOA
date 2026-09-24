package com.altermax.auth.application;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final JwtProperties properties;
    private final Clock clock;

    @Autowired
    public JwtService(JwtProperties properties) {
        this(properties, Clock.systemUTC());
    }

    JwtService(JwtProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    public String generate(UserDetails user) {
        Instant now = clock.instant();
        List<String> roles =
                user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.expiration())))
                .signWith(key())
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
    }

    public List<String> extractRoles(String token) {
        Object roles = parse(token).get("roles");
        if (!(roles instanceof List<?> list)) {
            return List.of();
        }
        return list.stream().map(Object::toString).toList();
    }

    public boolean isValid(String token, UserDetails user) {
        Claims claims = parse(token);
        return claims.getSubject().equals(user.getUsername())
                && claims.getExpiration().after(Date.from(clock.instant()));
    }

    public long expiresInSeconds() {
        return properties.expiration().toSeconds();
    }

    private SecretKey key() {
        byte[] bytes;
        try {
            bytes = java.util.Base64.getDecoder().decode(properties.secret());
        } catch (IllegalArgumentException ex) {
            bytes = properties.secret().getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(bytes);
    }
}
