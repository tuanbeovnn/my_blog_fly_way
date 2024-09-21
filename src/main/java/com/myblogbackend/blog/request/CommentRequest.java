package com.myblogbackend.blog.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = false)
public class CommentRequest {
    @NotBlank(message = "Content cannot be blank")
    private String content;
    @NotNull(message = "Post ID cannot be null")
    private UUID postId;
    private UUID parentCommentId;
}
