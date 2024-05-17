package com.myblogbackend.blog.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CommentRequest {
    @NotBlank(message = "Content cannot be blank")
    private String content;
    @NotNull(message = "Post ID cannot be null")
    private UUID postId;
    private UUID parentCommentId;
}
