package com.myblogbackend.blog.controllers;

import com.myblogbackend.blog.controllers.route.CommonRoutes;
import com.myblogbackend.blog.controllers.route.PostRoutes;
import com.myblogbackend.blog.enums.PostTag;
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
import java.util.concurrent.ExecutionException;

import static com.myblogbackend.blog.controllers.route.PostRoutes.PUBLIC_URL;

@RestController
@RequestMapping(CommonRoutes.BASE_API + CommonRoutes.VERSION)
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody @Valid final PostRequest postRequest) throws ExecutionException, InterruptedException {
        PostResponse post = postService.createPost(postRequest);
        return ResponseEntity.ok(post);
    }

    @GetMapping(PUBLIC_URL + PostRoutes.BASE_URL + "/{id}")
    public ResponseEntity<?> getPostById(@PathVariable(value = "id") final UUID id) {
        var post = postService.getPostById(id);
        return ResponseEntityBuilder
                .getBuilder()
                .setDetails(post)
                .build();
    }

    @GetMapping(PUBLIC_URL + PostRoutes.BASE_URL + "/feed")
    public ResponseEntity<?> getAllPostByFiltering(@RequestParam(value = "offset", defaultValue = "0") final Integer offset,
                                                   @RequestParam(value = "limit", defaultValue = "9") final Integer limit,
                                                   @RequestParam(value = "tags", required = false) final Set<PostTag> tags,
                                                   @RequestParam(value = "categoryId", required = false) final UUID categoryId,
                                                   @RequestParam(value = "userId", required = false) final UUID userId,
                                                   @RequestParam(defaultValue = "createdDate") final String sortField,
                                                   @RequestParam(defaultValue = "DESC") final String sortDirection) {

        var filter = PostFilterRequest.builder()
                .categoryId(categoryId)
                .userId(userId)
                .tags(tags)
                .sortField(sortField)
                .sortDirection(sortDirection)
                .build();

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
