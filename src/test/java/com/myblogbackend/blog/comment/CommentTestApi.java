package com.myblogbackend.blog.comment;

import com.myblogbackend.blog.models.CommentEntity;
import com.myblogbackend.blog.request.CommentRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.myblogbackend.blog.login.LoginTestApi.userEntityBasicInfo;
import static com.myblogbackend.blog.post.PostTestApi.makePostForSaving;

public final class CommentTestApi {
    public static CommentEntity makeCommentForSaving(final String content) {
        return CommentEntity.builder()
                .id(UUID.fromString("14ea3eb3-3d78-441d-b8f7-1be52e3861a4"))
                .content(content)
                .post(makePostForSaving("Title B", "Description B"))
                .status(true)
                .build();
    }

    public static CommentEntity makeCommentForSavingSamePostId(final String content) {
        return CommentEntity.builder()
                .id(UUID.fromString("86286271-7c2b-4fad-9125-a32e2ec9dc7c"))
                .content(content)
                .post(makePostForSaving("Title B", "Description B"))
                .status(true)
                .build();
    }

    public static CommentEntity makeCommentForDisable(final String content) {
        return CommentEntity.builder()
                .id(UUID.fromString("14ea3eb3-3d78-441d-b8f7-1be52e3861a4"))
                .content(content)
                .post(makePostForSaving("Title B", "Description B"))
                .status(false)
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
                makeCommentForSaving("what a detailed post"));
    }

    public static CommentEntity exsitingCommentEntity() {
        return CommentEntity.builder()
                .id(UUID.fromString("14ea3eb3-3d78-441d-b8f7-1be52e3861a4"))
                .content("existing comment entity")
                .post(makePostForSaving("Title B", "Description B"))
                .status(true)
                .user(userEntityBasicInfo())
                .build();
    }

    public static List<CommentEntity> createMockCommentList() {
        List<CommentEntity> comments = new ArrayList<>();
        // Mock data for comments
        CommentEntity comment1 = new CommentEntity();
        comment1.setId(UUID.fromString("55555555-5555-5555-5555-555555555555"));
        comment1.setContent("This is the first comment.");
        comment1.setStatus(true);
        comment1.setPost(makePostForSaving("springboot", "testing 1"));
        comment1.setParentComment(makeCommentForSavingSamePostId("parent comment"));

        CommentEntity comment2 = new CommentEntity();
        comment2.setId(UUID.fromString("66666666-6666-6666-6666-666666666666"));
        comment2.setContent("This is a reply to the first comment.");
        comment2.setStatus(true);
        comment2.setPost(makePostForSaving("microservices", "testing 2"));
        comment2.setParentComment(makeCommentForSavingSamePostId("parent comment")); 

        CommentEntity comment3 = new CommentEntity();
        comment3.setId(UUID.fromString("77777777-7777-7777-7777-777777777777"));
        comment3.setContent("This is another top-level comment.");
        comment3.setStatus(false);
        comment3.setPost(makePostForSaving("api integration testing", "testing 3"));
        comment3.setParentComment(makeCommentForSavingSamePostId("parent comment")); 

        comments.add(comment1);
        comments.add(comment2);
        comments.add(comment3);

        return comments;
    }
}
