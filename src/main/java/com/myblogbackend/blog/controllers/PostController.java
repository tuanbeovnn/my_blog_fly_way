package com.myblogbackend.blog.controllers;

import com.myblogbackend.blog.controllers.route.CommonRoutes;
import com.myblogbackend.blog.controllers.route.PostRoutes;
import com.myblogbackend.blog.dtos.PostElasticRequest;
import com.myblogbackend.blog.enums.PostTag;
import com.myblogbackend.blog.repositories.PostElasticsRepository;
import com.myblogbackend.blog.request.PostFilterRequest;
import com.myblogbackend.blog.request.PostRequest;
import com.myblogbackend.blog.response.PostResponse;
import com.myblogbackend.blog.response.ResponseEntityBuilder;
import com.myblogbackend.blog.services.PostElasticsService;
import com.myblogbackend.blog.services.PostService;
import com.myblogbackend.blog.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static com.myblogbackend.blog.controllers.route.PostRoutes.PUBLIC_URL;

@RestController
@RequestMapping(CommonRoutes.BASE_API + CommonRoutes.VERSION)
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final UserService userService;
    private final PostElasticsService postElasticsService;
    private  final PostElasticsRepository postElasticsRepository;

    @PostMapping("/posts")
    public ResponseEntity<?> createPost(@RequestBody @Valid final PostRequest postRequest) throws ExecutionException, InterruptedException {
        PostResponse post = postService.createPost(postRequest);
        return ResponseEntityBuilder
                .getBuilder()
                .setDetails(post)
                .build();
    }

    @PostMapping("/posts/draft")
    public ResponseEntity<?> savePostDraft(@RequestBody final PostRequest postRequest) {
        PostResponse post = postService.saveDraft(postRequest);
        return ResponseEntityBuilder
                .getBuilder()
                .setDetails(post)
                .build();
    }

    @GetMapping("/posts/draft")
    public ResponseEntity<?> getSavedDraft() {
        PostRequest draft = postService.getSavedDraft();
        return ResponseEntityBuilder
                .getBuilder()
                .setDetails(Objects.requireNonNullElseGet(draft, PostRequest::new))
                .build();
    }

    @GetMapping(PUBLIC_URL + PostRoutes.BASE_URL + "/post-tags")
    public ResponseEntity<?> getPostTags() {
        var listTags = Arrays.stream(PostTag.values())
                .map(PostTag::getType)
                .toList();
        return ResponseEntityBuilder
                .getBuilder()
                .setDetails(listTags)
                .build();
    }

    @GetMapping(PUBLIC_URL + PostRoutes.BASE_URL + "/slug/{slug}")
    public ResponseEntity<?> getPostBySlug(@PathVariable(value = "slug") final String slug) {
        var post = postService.getPostBySlug(slug);
        return ResponseEntityBuilder
                .getBuilder()
                .setDetails(post)
                .build();
    }

    @GetMapping(PUBLIC_URL + PostRoutes.BASE_URL + "/feed")
    public ResponseEntity<?> getAllPostByFiltering(@RequestParam(value = "offset", defaultValue = "0") final Integer offset,
                                                   @RequestParam(value = "limit", defaultValue = "9") final Integer limit,
                                                   @RequestParam(value = "tags", required = false) final Set<PostTag> tags,
                                                   @RequestParam(value = "categoryName", required = false) final String categoryName,
                                                   @RequestParam(value = "userId", required = false) final UUID userId,
                                                   @RequestParam(value = "userName", required = false) final String userName,
                                                   @RequestParam(defaultValue = "createdDate") final String sortField,
                                                   @RequestParam(defaultValue = "DESC") final String sortDirection) {

        var filter = PostFilterRequest.builder()
                .categoryName(categoryName)
                .userId(userId)
                .userName(userName)
                .tags(tags)
                .sortField(sortField)
                .sortDirection(sortDirection)
                .build();
        var pageable = PageRequest.of(offset, limit, Sort.Direction.fromString(sortDirection), sortField);

        var postFeeds = postService.getAllPostByFilter(pageable, filter);
        return ResponseEntityBuilder
                .getBuilder()
                .setDetails(postFeeds)
                .build();
    }

    @GetMapping(PUBLIC_URL + PostRoutes.BASE_URL + "/search-articles")
    public ResponseEntity<?> searchPost(
            @RequestParam(value = "offset", defaultValue = "0") final Integer offset,
            @RequestParam(value = "limit", defaultValue = "9") final Integer limit,
            @RequestParam(value = "search", required = true) final String search,
            @RequestParam(defaultValue = "createdDate") final String sortField,
            @RequestParam(defaultValue = "DESC") final String sortDirection) {

        var filter = PostFilterRequest.builder()
                .content(search)
                .title(search)
                .shortDescription(search)
                .sortField(sortField)
                .sortDirection(sortDirection)
                .build();
        var pageable = PageRequest.of(offset, limit, Sort.Direction.fromString(sortDirection), sortField);

        var postFeeds = postService.searchPosts(pageable, filter);

        return ResponseEntityBuilder
                .getBuilder()
                .setDetails(postFeeds)
                .build();
    }

    @PutMapping("/posts/{id}")
    public ResponseEntity<?> updatePost(@PathVariable(value = "id") final UUID id,
                                        @Valid @RequestBody final PostRequest postRequest) {
        var post = postService.updatePost(id, postRequest);
        return ResponseEntityBuilder
                .getBuilder()
                .setDetails(post)
                .build();
    }


    @PutMapping(PostRoutes.BASE_URL + "/disable/{postId}")
    public ResponseEntity<?> disablePost(@PathVariable final UUID postId) {
        postService.disablePost(postId);
        return ResponseEntityBuilder
                .getBuilder()
                .build();
    }

    @GetMapping("/posts/search")
    public ResponseEntity<List<PostResponse>> searchPosts(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        List<PostResponse> results = postService.searchPosts(query, page, size);
        return ResponseEntity.ok((results));
    }
    // manually sync MYSQL data to Elasticsearch
    @PostMapping("/sync")
    public ResponseEntity<?> syncPosts(@RequestBody final PostRequest postRequest) {
        postElasticsService.syncDatabaseToPostElastics();
        return ResponseEntity.ok("Elastics sync successful");
    }
    // test to verify elastics synced and accessible
    @GetMapping("/test-elastics")
    public ResponseEntity<?> testElastics() {
        return ResponseEntity.ok(postElasticsRepository.count());
    }
}
