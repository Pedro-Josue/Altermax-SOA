package com.altermax.auth.api;

/** Pequeno contrato interno para traduzir o subject autenticado em identidade persistida. */
public interface AuthenticatedUserProvider {
    Long requiredId(String username);
}
