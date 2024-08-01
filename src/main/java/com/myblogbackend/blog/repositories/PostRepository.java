package com.myblogbackend.blog.repositories;

import com.myblogbackend.blog.models.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository<PostEntity, UUID>, JpaSpecificationExecutor<PostEntity> {
    List<PostEntity> findAllByCategoryIdAndStatusTrue(UUID categoryId);

    Optional<PostEntity> findBySlug(String slug);

    Optional<PostEntity> findByIdAndUserId(UUID postId, UUID userId);

}
