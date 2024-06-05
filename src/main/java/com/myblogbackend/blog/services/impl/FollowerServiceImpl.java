package com.myblogbackend.blog.services.impl;

import com.myblogbackend.blog.enums.FollowType;
import com.myblogbackend.blog.exception.commons.BlogRuntimeException;
import com.myblogbackend.blog.exception.commons.ErrorCode;
import com.myblogbackend.blog.models.FollowersEntity;
import com.myblogbackend.blog.repositories.FollowerRepository;
import com.myblogbackend.blog.repositories.UsersRepository;
import com.myblogbackend.blog.services.FollowerService;
import com.myblogbackend.blog.utils.JWTSecurityUtil;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FollowerServiceImpl implements FollowerService {
    private static final Logger logger = LogManager.getLogger(FollowerServiceImpl.class);

    private final FollowerRepository followerRepository;
    private final UsersRepository usersRepository;

    @Override
    @Transactional
    public void persistOrDelete(final UUID userIdToFollow) {
        var signedInUser = JWTSecurityUtil.getJWTUserInfo().orElseThrow();
        var existingFollower = followerRepository.findByFollowerIdAndFollowedUserId(signedInUser.getId(), userIdToFollow);
        var followedUser = usersRepository.findById(userIdToFollow)
                .orElseThrow(() -> new BlogRuntimeException(ErrorCode.ID_NOT_FOUND));

        existingFollower.ifPresentOrElse(follower -> {
            followerRepository.delete(follower);
            followedUser.setFollowers(Math.max(0, followedUser.getFollowers() - 1));
            usersRepository.save(followedUser);
            logger.info("User {} unfollowed user {} successfully", signedInUser.getId(), userIdToFollow);
        }, () -> {
            var user = usersRepository.findById(signedInUser.getId()).orElseThrow();
            var newFollower = new FollowersEntity();
            newFollower.setFollower(user);
            newFollower.setFollowedUser(followedUser);
            newFollower.setType(FollowType.FOLLOW);
            followerRepository.save(newFollower);
            followedUser.setFollowers(followedUser.getFollowers() + 1);
            usersRepository.save(followedUser);
            logger.info("User {} followed user {} successfully", signedInUser.getId(), userIdToFollow);
        });
    }
}
