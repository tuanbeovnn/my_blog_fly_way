package com.myblogbackend.blog.repositories;

import com.myblogbackend.blog.documents.PostElastic;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PostElasticsRepository extends ElasticsearchRepository<PostElastic, UUID> {
}
