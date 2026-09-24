package com.altermax.auth.application;

import com.altermax.auth.api.LoginRequest;
import com.altermax.auth.api.LoginResponse;
import com.altermax.shared.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthenticationService(
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(), request.password()));
        } catch (AuthenticationException ex) {
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Usuario ou senha invalidos.");
        }
        var user = userDetailsService.loadUserByUsername(request.username());
        return new LoginResponse(
                jwtService.generate(user),
                "Bearer",
                jwtService.expiresInSeconds(),
                user.getUsername(),
                user.getAuthorities().stream().map(authority -> authority.getAuthority()).toList());
    }
}
