package com.myblogbackend.blog.response;

import com.myblogbackend.blog.enums.FollowType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPostFavoriteResponse {
    private UUID userId;
    private String username;
    private Long postCount;
    private Long totalFavorites;
    private FollowType followType;
    private String avatarUrl;
    private UserResponse userResponse;
}
