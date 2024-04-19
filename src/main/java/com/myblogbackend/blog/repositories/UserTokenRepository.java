package com.myblogbackend.blog.repositories;


import com.myblogbackend.blog.models.UserEntity;
import com.myblogbackend.blog.models.UserVerificationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserTokenRepository extends JpaRepository<UserVerificationTokenEntity, UUID> {
    UserVerificationTokenEntity findByVerificationToken(String verToken);

    UserVerificationTokenEntity findByUser(UserEntity user);
    void deleteByUserId(UUID userId);
}
