package com.altermax.auth.infrastructure;

import com.altermax.auth.api.AuthenticatedUserProvider;
import com.altermax.shared.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class AuthUserLookup implements AuthenticatedUserProvider {
    private final UserAccountRepository repository;

    public AuthUserLookup(UserAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public Long requiredId(String username) {
        return repository
                .findByUsername(username)
                .map(account -> account.getId())
                .orElseThrow(
                        () ->
                                new ApiException(
                                        HttpStatus.UNAUTHORIZED,
                                        "UNAUTHORIZED",
                                        "Usuario autenticado nao encontrado."));
    }
}
