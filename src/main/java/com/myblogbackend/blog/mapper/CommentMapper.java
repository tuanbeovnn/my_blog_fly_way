package com.myblogbackend.blog.mapper;

import com.myblogbackend.blog.models.CommentEntity;
import com.myblogbackend.blog.request.CommentRequest;
import com.myblogbackend.blog.response.CommentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    CommentEntity toCommentEntity(CommentRequest commentRequest);

    @Mapping(source = "commentEntity.user.userName", target = "userName")
    @Mapping(source = "commentEntity.user.name", target = "name")
    @Mapping(source = "commentEntity.post.id", target = "postId")
    @Mapping(source = "commentEntity.user.id", target = "userId")
    @Mapping(source = "commentEntity.user.profile.avatarUrl", target = "avatar")
    @Mapping(source = "commentEntity.modifiedDate", target = "modifiedDate")
    @Mapping(source = "commentEntity.parentComment.id", target = "parentId")
    CommentResponse toCommentResponse(CommentEntity commentEntity);
}
