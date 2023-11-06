package com.myblogbackend.blog.services;

import com.myblogbackend.blog.models.UserEntity;
import com.myblogbackend.blog.models.UserVerificationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<UserVerificationTokenEntity, Long> {
    UserVerificationTokenEntity findByVerificationToken(String verToken);

    UserVerificationTokenEntity findByUser(UserEntity userEntity);

}
