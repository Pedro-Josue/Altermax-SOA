package com.altermax.auth.api;

import com.altermax.auth.application.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationService service;

    public AuthController(AuthenticationService service) {
        this.service = service;
    }

    @Operation(
            summary = "Autentica um usuario",
            security = {},
            responses = {
                @ApiResponse(responseCode = "200", description = "Credenciais validas"),
                @ApiResponse(responseCode = "401", description = "Credenciais invalidas")
            })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid
            @RequestBody LoginRequest request) {
        return ResponseEntity.ok(service.login(request));
    }
}
