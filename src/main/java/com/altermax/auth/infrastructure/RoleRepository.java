package com.altermax.auth.infrastructure;

import com.altermax.auth.domain.Role;
import com.altermax.auth.domain.RoleEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(Role name);
}
