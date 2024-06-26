package com.myblogbackend.blog.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CommentResponse {
    private String content;
    private String userName;
    private UUID postId;
}
