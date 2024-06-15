package com.myblogbackend.blog.repositories;

import com.myblogbackend.blog.models.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository<PostEntity, UUID>, JpaSpecificationExecutor<PostEntity> {
    Page<PostEntity> findAllByCategoryIdAndStatusTrueOrderByCreatedDateDesc(Pageable pageable, UUID categoryId);

    Page<PostEntity> findAllByUserId(UUID userId, Pageable pageable);

    Page<PostEntity> findAllByUserIdAndStatusTrueOrderByCreatedDateDesc(UUID userId, Pageable pageable);

    List<PostEntity> findAllByCategoryIdAndStatusTrue(UUID categoryId);

    Optional<PostEntity> findBySlug(String slug);

}
