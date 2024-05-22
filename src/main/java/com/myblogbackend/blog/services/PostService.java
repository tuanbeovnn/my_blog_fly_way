package com.myblogbackend.blog.services;

import com.myblogbackend.blog.pagination.PaginationPage;
import com.myblogbackend.blog.request.PostFilterRequest;
import com.myblogbackend.blog.request.PostRequest;
import com.myblogbackend.blog.response.PostResponse;

import java.util.UUID;

public interface PostService {
    PaginationPage<PostResponse> getAllPostsByUserId(UUID userId, Integer offset, Integer limited);

//    PaginationPage<PostResponse> getAllPostsByCategoryId(Integer offset, Integer limited, UUID categoryId);

    PostResponse getPostById(UUID id);

    PostResponse createPost(PostRequest postRequest);

    PostResponse updatePost(UUID id, PostRequest postRequest);

    PaginationPage<PostResponse> getAllPostByFilter(Integer offset, Integer limited, PostFilterRequest filter);


}
