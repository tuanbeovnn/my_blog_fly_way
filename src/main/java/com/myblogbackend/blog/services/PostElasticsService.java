package com.myblogbackend.blog.services;

import com.myblogbackend.blog.dtos.PostElasticRequest;
import com.myblogbackend.blog.request.PostRequest;

import java.util.List;
import java.util.UUID;

public interface PostElasticsService {
    void savePostElastic(PostElasticRequest postElasticRequest);
    void updatePostElastics(UUID postId, PostElasticRequest postElasticRequest);
    void syncDatabaseToPostElastics();
     List<PostElasticRequest> searchPostElastics(String query, int page, int size);
}
