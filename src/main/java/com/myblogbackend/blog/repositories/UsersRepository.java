package com.myblogbackend.blog.repositories;

import com.myblogbackend.blog.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface UsersRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);

    @Query(value = "select * from users u where u.active = true", nativeQuery = true)
    List<UserEntity> findAllByActiveIsTrue();

    Optional<UserEntity> findByUserName(String email);

}