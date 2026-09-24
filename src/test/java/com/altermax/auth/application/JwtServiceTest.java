package com.altermax.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;

class JwtServiceTest {
    private static final String SECRET =
            "YWx0ZXJtYXgtdGVzdC1zZWNyZXQtcXVlLXRlbS1tYWlzLWRlLTMydC1ieXRlcw==";

    @Test
    void geraValidaECarregaRoles() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        var service =
                new JwtService(
                        new JwtProperties(SECRET, Duration.ofMinutes(5)),
                        Clock.fixed(now, ZoneOffset.UTC));
        var user = User.withUsername("maria").password("x").roles("USER").build();
        String token = service.generate(user);
        var claims = service.parse(token);
        assertThat(claims.getSubject()).isEqualTo("maria");
        assertThat(claims.get("roles", java.util.List.class)).contains("ROLE_USER");
        assertThat(claims.getIssuedAt()).isEqualTo(java.util.Date.from(now));
        assertThat(claims.getExpiration()).isEqualTo(java.util.Date.from(now.plusSeconds(300)));
        assertThat(claims).doesNotContainKeys("password");
        assertThat(service.isValid(token, user)).isTrue();
    }

    @Test
    void rejeitaTokenComAssinaturaAdulterada() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        var service =
                new JwtService(
                        new JwtProperties(SECRET, Duration.ofMinutes(5)),
                        Clock.fixed(now, ZoneOffset.UTC));
        String token =
                service.generate(User.withUsername("maria").password("x").roles("USER").build());
        char replacement = token.endsWith("a") ? 'b' : 'a';
        String tampered = token.substring(0, token.length() - 1) + replacement;

        assertThatThrownBy(() -> service.parse(tampered)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejeitaTokenExpirado() {
        var service =
                new JwtService(
                        new JwtProperties(SECRET, Duration.ofSeconds(1)),
                        Clock.fixed(Instant.now().minusSeconds(5), ZoneOffset.UTC));
        String token =
                service.generate(User.withUsername("maria").password("x").roles("USER").build());

        assertThatThrownBy(() -> service.parse(token)).isInstanceOf(ExpiredJwtException.class);
    }
}
