package com.myblogbackend.blog.repositories;

import com.myblogbackend.blog.models.FollowersEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface FollowersRepository extends JpaRepository<FollowersEntity, UUID> {
    List<FollowersEntity> findByFollowedUserId(UUID followedUserId);
}
