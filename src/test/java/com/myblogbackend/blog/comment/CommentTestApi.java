package com.myblogbackend.blog.comment;

import com.myblogbackend.blog.models.CommentEntity;
import com.myblogbackend.blog.request.CommentRequest;
import com.myblogbackend.blog.request.PostRequest;

import javax.xml.stream.events.Comment;
import java.util.List;
import java.util.UUID;

import static com.myblogbackend.blog.post.PostTestApi.makePostForSaving;

public final class CommentTestApi {
    public static CommentEntity makeCommentForSaving(final String content)
    {
        return CommentEntity.builder()
                .id(UUID.fromString("14ea3eb3-3d78-441d-b8f7-1be52e3861a4"))
                .content(content)
                .post(makePostForSaving("Title B", "Description B"))
                .status(true)
                .build();
    }

    public static CommentRequest prepareCommentRequest() {
        return CommentRequest.builder()
                .content("this post is helpful")
                .postId(UUID.fromString("c8f3d140-5fdd-4055-8530-2c5358da7952"))
                .parentCommentId(null)
                .build();
    }

    public static List<CommentEntity> prepareCommentListSaving() {
        return List.of(
                makeCommentForSaving("this post is great"),
                makeCommentForSaving("what a detailed post")
                );
    }

    public static CommentEntity exsitingCommentEntity()
    {
        return CommentEntity.builder()
                .id(UUID.fromString("14ea3eb3-3d78-441d-b8f7-1be52e3861a4"))
                .content("existing comment entity")
                .post(makePostForSaving("Title B", "Description B"))
                .status(true)
                .build();
    }
}
