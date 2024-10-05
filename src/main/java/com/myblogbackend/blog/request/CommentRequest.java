package com.myblogbackend.blog.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class CommentRequest {
    @NotEmpty(message = "Content cannot be blank")
    private String content;
    @NotNull(message = "Post ID cannot be null")
    private UUID postId;
    private UUID parentCommentId;
}
