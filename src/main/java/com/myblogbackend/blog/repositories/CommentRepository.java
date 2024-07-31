package com.myblogbackend.blog.repositories;

import com.myblogbackend.blog.models.CommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, UUID> {
    Page<CommentEntity> findByPostIdAndStatusTrueOrderByCreatedDateDesc(UUID postId, Pageable pageable);

    List<CommentEntity> findByParentComment(CommentEntity parentComment);

    List<CommentEntity> findByPostId(UUID postId);

    Optional<CommentEntity> findByIdAndUserId(UUID commentId, UUID userId);

    // Fetch only parent comments with pagination
    @Query("SELECT c FROM CommentEntity c WHERE c.post.id = :postId AND c.parentComment IS NULL AND c.status = true ORDER BY c.createdDate DESC")
    Page<CommentEntity> findParentCommentsByPostIdAndStatusTrue(@Param("postId") UUID postId, Pageable pageable);

    // Fetch replies by parent comment IDs
    @Query("SELECT c FROM CommentEntity c WHERE c.parentComment.id IN :parentCommentIds")
    List<CommentEntity> findAllByParentCommentIdIn(@Param("parentCommentIds") List<UUID> parentCommentIds);

}
