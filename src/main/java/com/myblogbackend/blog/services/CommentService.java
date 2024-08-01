package com.myblogbackend.blog.services;

import com.myblogbackend.blog.pagination.PageList;
import com.myblogbackend.blog.request.CommentRequest;
import com.myblogbackend.blog.response.CommentResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CommentService {
    CommentResponse createComment(CommentRequest commentRequest);

    PageList<CommentResponse> getListCommentsByPostId(Pageable pageable, UUID postId);

    CommentResponse updateComment(UUID commentId, CommentRequest commentRequest);

    void disableComment(UUID commentId);

}
