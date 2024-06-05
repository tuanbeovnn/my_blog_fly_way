package com.myblogbackend.blog.repositories;

import com.myblogbackend.blog.models.FollowersEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;


public interface FollowerRepository extends JpaRepository<FollowersEntity, UUID> {
    Optional<FollowersEntity> findByFollowerIdAndFollowedUserId(UUID followerId, UUID followedUserId);

}
