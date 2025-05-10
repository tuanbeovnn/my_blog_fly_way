package com.myblogbackend.blog.services.impl;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.myblogbackend.blog.documents.PostElastic;
import com.myblogbackend.blog.dtos.PostElasticRequest;
import com.myblogbackend.blog.models.PostEntity;
import com.myblogbackend.blog.repositories.PostElasticsRepository;
import com.myblogbackend.blog.repositories.PostRepository;
import com.myblogbackend.blog.services.PostElasticsService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class PostElasticsServiceImpl implements PostElasticsService {
    private static final Logger log = LogManager.getLogger(PostElasticsServiceImpl.class);
    private final PostElasticsRepository postElasticsRepository;
    private final PostRepository postRepository;
    private final ElasticsearchClient elasticsearchClient;

    @PostConstruct
    public void init() {
        try {
            syncDatabaseToPostElastics();
        } catch (Exception e) {
            log.error(e);
        }

    }
    @Override
    public void savePostElastic(final PostElasticRequest postElasticRequest) {
        PostElastic postElastic = new PostElastic();

        postElastic.setId(postElasticRequest.getId());
        postElastic.setTitle(postElasticRequest.getTitle());
        postElastic.setShortDescription(postElasticRequest.getShortDescription());
        postElastic.setContent(postElasticRequest.getContent());
        postElasticsRepository.save(postElastic);
        log.info("Saved to Elasticsearch: {}", postElastic);
    }

    @Retryable(value = {ElasticsearchException.class}, maxAttempts = 3, backoff = @Backoff(delay = 5000))
    @Override
    public void updatePostElastics(final UUID postId, final PostElasticRequest postElasticRequest) {
    try {
        if (postId == null || postElasticRequest == null){
            log.error("Invalid input for updating post in Elasticsearch: postId={}, postRequest={}",
                    postId, postElasticRequest);
            throw new IllegalArgumentException("Post ID and request cannot be null");
        }
        Optional<PostElastic> foundPostElastic = postElasticsRepository.findById(postId);
        if(!foundPostElastic.isPresent()){
            log.error("Post ID {} not found in Elasticsearch", postId);
            throw new IllegalArgumentException("Post ID not found in Elasticsearch");
        }
        PostElastic postElastic = foundPostElastic.get();
        postElastic.setTitle(postElasticRequest.getTitle());
        postElastic.setContent(postElasticRequest.getContent());
        postElastic.setShortDescription(postElasticRequest.getShortDescription());
        postElasticsRepository.save(postElastic);
        log.info("Updated to Elasticsearch: {}", postElastic);
    } catch (Exception e) {
        log.error("Failed to update post in Elasticsearch with id: {}. Error: {}",
                postId, e.getMessage());
        throw new RuntimeException("Failed to update post in Elasticsearch", e);
    }
    }

    @Override
    public void syncDatabaseToPostElastics() {
        try {
            long elasticCount = postElasticsRepository.count();
            if(elasticCount > 0){
                log.info("Elastic count is {}", elasticCount);
            }
            int pageSize = 100;
            Pageable pageable = PageRequest.of(0, pageSize);
            long totalPosts = postRepository.count();
            long syncedPosts = 0;
            log.info("Starting sync of {} posts from MySQL to Elasticsearch", totalPosts);

            while((long) pageable.getPageNumber() * pageSize < totalPosts){
                Page<PostEntity> postPage = postRepository.findAll(pageable);
                List<PostElastic> postElasticList = postPage.getContent().stream()
                        .map(post -> new PostElastic(
                                post.getId(),
                                post.getTitle(),
                                post.getContent(),
                                post.getShortDescription()
                        )).toList();
                postElasticsRepository.saveAll(postElasticList);
                syncedPosts += postElasticList.size();
                log.info("Synced {} of {} posts to Elasticsearch", syncedPosts, totalPosts);
                // continue to next page
                pageable = PageRequest.of(pageable.getPageNumber() + 1, pageSize);
            }
            log.info("Successfully synced {} posts to Elasticsearch", syncedPosts);
        } catch (Exception e) {
            log.error("Failed to sync posts to Elasticsearch: {}", e.getMessage());
            throw new RuntimeException("Elasticsearch sync failed", e);
        }
    }

    @Override
    public List<PostElasticRequest> searchPostElastics(final String query, final int page, final int size) {
        log.info("Searching products with fuzziness for keyword: {}", query);

        SearchResponse<PostElasticRequest> searchResponse;
        try {
            searchResponse = elasticsearchClient.search(s -> {
                        s.index("posts");
                        if (!StringUtils.hasText(query)) {
                            log.debug("No keyword provided, performing match all query.");
                            s.query(q -> q.matchAll(m -> m));
                        } else {
                            log.debug("Building fuzzy search query for keyword: {}", query);
                            s.query(q -> q
                                    .bool(b -> b
                                            .should(m -> m.match(t -> t.field("title").query(query).boost(3.0F).fuzziness("AUTO")))
                                            .should(m -> m.match(t -> t.field("content").query(query).boost(2.0F).fuzziness("AUTO")))
                                            .should(m -> m.match(t -> t.field("shortDescription").query(query).boost(1.5F).fuzziness("AUTO")))
                                    )
                            );
                        }
                        return s;
                    },
                    PostElasticRequest.class
            );

            List<PostElasticRequest> postDtos = searchResponse.hits().hits().stream()
                    .map(Hit::source)
                    .toList();

            log.info("Found {} fuzzy search results for keyword: {}", postDtos.size(), query);
            return postDtos;
        } catch (ElasticsearchException e) {
            log.error("Elasticsearch query failed: {}", e.getMessage(), e);
            return List.of();
        } catch (IOException e) {
            log.error("IO Exception in Elasticsearch search: {}", e.getMessage(), e);
            return List.of();
        }
    }
}