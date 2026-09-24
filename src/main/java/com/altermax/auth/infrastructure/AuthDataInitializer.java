package com.altermax.auth.infrastructure;

import com.altermax.auth.domain.Role;
import com.altermax.auth.domain.RoleEntity;
import com.altermax.auth.domain.UserAccount;
import java.util.Set;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(1)
public class AuthDataInitializer implements ApplicationRunner {
    private final RoleRepository roles;
    private final UserAccountRepository users;
    private final PasswordEncoder encoder;

    public AuthDataInitializer(
            RoleRepository roles, UserAccountRepository users, PasswordEncoder encoder) {
        this.roles = roles;
        this.users = users;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        RoleEntity userRole =
                roles.findByName(Role.ROLE_USER)
                        .orElseGet(() -> roles.save(new RoleEntity(Role.ROLE_USER)));
        RoleEntity adminRole =
                roles.findByName(Role.ROLE_ADMIN)
                        .orElseGet(() -> roles.save(new RoleEntity(Role.ROLE_ADMIN)));
        if (users.findByUsername("user").isEmpty()) {
            users.save(new UserAccount("user", encoder.encode("user123"), true, Set.of(userRole)));
        }
        if (users.findByUsername("admin").isEmpty()) {
            users.save(
                    new UserAccount(
                            "admin",
                            encoder.encode("admin123"),
                            true,
                            Set.of(userRole, adminRole)));
        }
    }
}
