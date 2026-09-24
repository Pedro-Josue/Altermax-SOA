package com.altermax.history.api;

import com.altermax.auth.api.AuthenticatedUserProvider;
import com.altermax.history.application.ComparisonHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comparison-history")
public class ComparisonHistoryController {
    private final ComparisonHistoryService service;
    private final AuthenticatedUserProvider users;

    public ComparisonHistoryController(
            ComparisonHistoryService service, AuthenticatedUserProvider users) {
        this.service = service;
        this.users = users;
    }

    @Operation(summary = "Lista o proprio historico; ADMIN lista todos")
    @GetMapping
    public List<ComparisonHistoryView> list(Authentication authentication) {
        return service.list(users.requiredId(authentication.getName()), isAdmin(authentication));
    }

    @Operation(summary = "Consulta snapshot de uma comparacao permitida")
    @GetMapping("/{id}")
    public ComparisonHistoryView get(@PathVariable Long id, Authentication authentication) {
        return service.get(id, users.requiredId(authentication.getName()), isAdmin(authentication));
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}
