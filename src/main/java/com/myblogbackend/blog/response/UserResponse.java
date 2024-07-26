package com.myblogbackend.blog.response;

import com.myblogbackend.blog.enums.FollowType;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String email;
    private String name;
    private Long followers = 0L;
    private List<UserFollowingResponse> usersFollowing = new ArrayList<>();
    private ProfileResponseDTO profile;
    private FollowType followType;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProfileResponseDTO {
        private String bio;
        private String website;
        private String location;
        private String avatarUrl;
        private SocialLinksDTO social;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SocialLinksDTO {
        private String twitter;
        private String linkedin;
        private String github;
    }

}
