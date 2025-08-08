package com.myblogbackend.blog.services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.myblogbackend.blog.enums.FollowType;
import com.myblogbackend.blog.models.FollowersEntity;
import com.myblogbackend.blog.repositories.FollowersRepository;
import com.myblogbackend.blog.repositories.UsersRepository;
import com.myblogbackend.blog.response.UserFollowingResponse;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserFollowingService {

    private final FollowersRepository followersRepository;
    private final UsersRepository usersRepository;

    public FollowType getUserFollowingType(final UUID followerId, final UUID followedUserId) {
        if (followerId == null || followedUserId == null) {
            return FollowType.UNFOLLOW;
        }
        return followersRepository.findByFollowerIdAndFollowedUserId(followerId, followedUserId)
                .map(FollowersEntity::getType)
                .orElse(FollowType.UNFOLLOW);
    }

    public @NotNull List<UserFollowingResponse> getUserFollowingResponses(final List<FollowersEntity> followers) {
        if (followers == null || followers.isEmpty()) {
            return List.of();
        }

        return followers.stream()
                .map(follower -> {
                    var followerUser = usersRepository.findById(follower.getFollower().getId())
                            .orElse(null);
                    if (followerUser == null) {
                        return null;
                    }
                    return UserFollowingResponse.builder()
                            .id(followerUser.getId())
                            .name(followerUser.getName())
                            .userName(followerUser.getUserName())
                            .build();
                })
                .filter(response -> response != null)
                .toList();
    }

    public List<FollowersEntity> getFollowersByUserId(final UUID userId) {
        return followersRepository.findByFollowedUserId(userId);
    }
}
