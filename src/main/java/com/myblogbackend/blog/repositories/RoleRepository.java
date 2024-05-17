package com.myblogbackend.blog.repositories;

import com.myblogbackend.blog.enums.RoleName;
import com.myblogbackend.blog.models.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {
    Optional<RoleEntity> findByName(RoleName roleName);
}
