package com.myblogbackend.blog.repositories;

import com.myblogbackend.blog.models.ViewersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ViewerRepository extends JpaRepository<ViewersEntity, UUID> {
    @Modifying
    @Query("UPDATE ViewersEntity SET viewCounter = viewCounter + 1 where postId = :postId")
    void updateViewer(@Param("postId") UUID postId);

    Optional<ViewersEntity> findByPostId(UUID postId);

}
