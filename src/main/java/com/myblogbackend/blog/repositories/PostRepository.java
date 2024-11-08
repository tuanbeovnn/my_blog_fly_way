package com.myblogbackend.blog.repositories;

import com.myblogbackend.blog.models.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository<PostEntity, UUID>, JpaSpecificationExecutor<PostEntity> {
    List<PostEntity> findAllByCategoryIdAndStatusTrueAndApprovedTrue(UUID categoryId);

    Optional<PostEntity> findBySlug(String slug);

    Optional<PostEntity> findByIdAndUserId(UUID postId, UUID userId);

    // Custom method to find users with many posts and high favorites, returning List<Object[]>
    @Query("SELECT u.id, u.userName, COUNT(p.id) AS postCount, SUM(p.favourite) AS totalFavourites " +
            "FROM PostEntity p " +
            "JOIN p.user u " +
            "WHERE p.approved = TRUE " +
            "GROUP BY u.id, u.userName " +
            "HAVING COUNT(p.id) > :postThreshold AND SUM(p.favourite) > :favoritesThreshold " +
            "ORDER BY postCount DESC, totalFavourites DESC")
    List<Object[]> findUsersWithManyPostsAndHighFavorites(@Param("postThreshold") long postThreshold,
                                                          @Param("favoritesThreshold") long favoritesThreshold);

}
