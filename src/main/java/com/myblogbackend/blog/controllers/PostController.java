package com.myblogbackend.blog.controllers;

import com.myblogbackend.blog.controllers.route.CommonRoutes;
import com.myblogbackend.blog.controllers.route.PostRoutes;
import com.myblogbackend.blog.request.PostFilterRequest;
import com.myblogbackend.blog.request.PostRequest;
import com.myblogbackend.blog.response.PostResponse;
import com.myblogbackend.blog.response.ResponseEntityBuilder;
import com.myblogbackend.blog.services.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping(CommonRoutes.BASE_API + CommonRoutes.VERSION + PostRoutes.BASE_URL)
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping("/posts")
    public ResponseEntity<?> createPost(@RequestBody @Valid final PostRequest postRequest) {
        PostResponse post = postService.createPost(postRequest);
        return ResponseEntity.ok(post);
    }

    @GetMapping("/user")
    public ResponseEntity<?> getAllPostsByUserId(@RequestParam(name = "offset", defaultValue = "0") final Integer offset,
                                                 @RequestParam(name = "limit", defaultValue = "10") final Integer limit) {
        var postList = postService.getAllPostsByUserId(offset, limit);
        return ResponseEntityBuilder
                .getBuilder()
                .setDetails(postList)
                .build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPostById(@PathVariable(value = "id") final UUID id) {
        var post = postService.getPostById(id);
        return ResponseEntity.ok(post);
    }

    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<?> getAllPostsByCategoryId(@RequestParam(name = "offset", defaultValue = "0") final Integer offset,
                                                     @RequestParam(name = "limit", defaultValue = "10") final Integer limit,
                                                     @PathVariable(value = "categoryId") final UUID categoryId) {
        var postList = postService.getAllPostsByCategoryId(offset, limit, categoryId);
        return ResponseEntityBuilder
                .getBuilder()
                .setDetails(postList)
                .build();
    }

    @GetMapping("/feed")
    public ResponseEntity<?> getAllPostByFiltering(@RequestParam(value = "offset", defaultValue = "0") final Integer offset,
                                                   @RequestParam(value = "limit", defaultValue = "10") final Integer limit,
                                                   @RequestParam(value = "tags", required = false) final Set<String> tags,
                                                   @RequestParam(defaultValue = "createdDate") final String sortField,
                                                   @RequestParam(defaultValue = "DESC") final String sortDirection) {
        var filter = new PostFilterRequest(tags, sortField, sortDirection);

        var postFeeds = postService.getAllPostByFilter(offset, limit, filter);
        return ResponseEntityBuilder
                .getBuilder()
                .setDetails(postFeeds)
                .build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(@PathVariable(value = "id") final UUID id,
                                        final PostRequest postRequest) {
        var post = postService.updatePost(id, postRequest);
        return ResponseEntity.ok(post);
    }

}
