package com.myblogbackend.blog.controllers;

import static com.myblogbackend.blog.controllers.route.PostRoutes.PUBLIC_URL;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.myblogbackend.blog.controllers.route.CommonRoutes;
import com.myblogbackend.blog.controllers.route.PostRoutes;
import com.myblogbackend.blog.enums.PostTag;
import com.myblogbackend.blog.request.PostFilterRequest;
import com.myblogbackend.blog.request.PostRequest;
import com.myblogbackend.blog.response.PostResponse;
import com.myblogbackend.blog.response.ResponseEntityBuilder;
import com.myblogbackend.blog.services.PostService;
import com.myblogbackend.blog.services.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(CommonRoutes.BASE_API + CommonRoutes.VERSION)
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final UserService userService;

    @PostMapping("/posts")
    public ResponseEntity<?> createPost(@RequestBody @Valid final PostRequest postRequest)
            throws ExecutionException, InterruptedException {
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
    public ResponseEntity<?> getAllPostByFiltering(
            @RequestParam(value = "offset", defaultValue = "0") final Integer offset,
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

        var postFeeds = postService.relatedPosts(pageable, filter);

        return ResponseEntityBuilder
                .getBuilder()
                .setDetails(postFeeds)
                .build();
    }

    @GetMapping(PUBLIC_URL + PostRoutes.BASE_URL + "/related/{postId}")
    public ResponseEntity<?> getRelatedPosts(
            @PathVariable(value = "postId") final UUID postId,
            @RequestParam(value = "offset", defaultValue = "0") final Integer offset,
            @RequestParam(value = "limit", defaultValue = "20") final Integer limit,
            @RequestParam(value = "sortBy", defaultValue = "createdDate") final String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "DESC") final String sortDirection,
            @RequestParam(value = "context", required = false) final String context) { // reading, browsing, search

        // Validate and sanitize parameters
        int validatedOffset = Math.max(0, offset);
        int validatedLimit = Math.max(1, Math.min(100, limit)); // Ensure limit is between 1 and 100

        var sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        var pageable = PageRequest.of(validatedOffset, validatedLimit, sort);
        var relatedPosts = postService.getRelatedPosts(postId, pageable);

        // TODO: Track analytics for related post requests
        // analyticsService.trackRelatedPostRequest(postId, context,
        // relatedPosts.getTotalRecords());

        return ResponseEntityBuilder
                .getBuilder()
                .setDetails(relatedPosts)
                .setMessage("Related posts retrieved successfully")
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

}
