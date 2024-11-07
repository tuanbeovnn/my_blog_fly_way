package com.myblogbackend.blog.services;

import com.myblogbackend.blog.pagination.PageList;
import com.myblogbackend.blog.request.PostFilterRequest;
import com.myblogbackend.blog.request.PostRequest;
import com.myblogbackend.blog.response.PostResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public interface PostService {

//    PaginationPage<PostResponse> getAllPostsByCategoryId(Integer offset, Integer limited, UUID categoryId);

    PostResponse getPostBySlug(String slug);

    PostResponse createPost(PostRequest postRequest) throws ExecutionException, InterruptedException;

    PostResponse updatePost(UUID id, PostRequest postRequest);

    PageList<PostResponse> getAllPostByFilter(final Pageable pageable, PostFilterRequest filter);


    PageList<PostResponse> searchPosts(final Pageable pageable, final PostFilterRequest filter);

    void disablePost(UUID postId);

    PostResponse saveDraft(PostRequest postRequest);

    PostRequest getSavedDraft();

    PageList<PostResponse> getAllPostByStatus(final Pageable pageable, PostFilterRequest filter);

    PostResponse approvePost(UUID id);



}
