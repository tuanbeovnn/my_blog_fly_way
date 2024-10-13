package com.myblogbackend.blog.repositories;


import com.myblogbackend.blog.models.UserVerificationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserTokenRepository extends JpaRepository<UserVerificationTokenEntity, UUID> {
    Optional<UserVerificationTokenEntity> findByVerificationToken(String verToken);

    void deleteByUserId(UUID userId);


}
