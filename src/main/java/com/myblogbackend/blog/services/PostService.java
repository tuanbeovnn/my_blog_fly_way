package com.myblogbackend.blog.services;

import com.myblogbackend.blog.pagination.PaginationPage;
import com.myblogbackend.blog.request.PostFilterRequest;
import com.myblogbackend.blog.request.PostRequest;
import com.myblogbackend.blog.response.PostResponse;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public interface PostService {

//    PaginationPage<PostResponse> getAllPostsByCategoryId(Integer offset, Integer limited, UUID categoryId);

    PostResponse getPostById(UUID id);

    PostResponse getPostBySlug(String slug);

    PostResponse createPost(PostRequest postRequest) throws ExecutionException, InterruptedException;

    PostResponse updatePost(UUID id, PostRequest postRequest);

    PaginationPage<PostResponse> getAllPostByFilter(Integer offset, Integer limited, PostFilterRequest filter);


    PaginationPage<PostResponse> getRelatedPosts(final Integer offset, final Integer limited, final PostFilterRequest filter);

    void disablePost(UUID postId);


}
