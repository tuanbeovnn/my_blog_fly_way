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
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

import static com.myblogbackend.blog.controllers.route.PostRoutes.PUBLIC_URL;

@RestController
@RequestMapping(CommonRoutes.BASE_API + CommonRoutes.VERSION)
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody @Valid final PostRequest postRequest) {
        PostResponse post = postService.createPost(postRequest);
        return ResponseEntity.ok(post);
    }

    @GetMapping(PUBLIC_URL + PostRoutes.BASE_URL + "/user")
    public ResponseEntity<?> getAllPostsByUserId(@RequestParam(name = "offset", defaultValue = "0") final Integer offset,
                                                 @RequestParam(name = "limit", defaultValue = "10") final Integer limit,
                                                 @RequestParam(name = "userId", required = true) final UUID userId) {
        var postList = postService.getAllPostsByUserId(userId, offset, limit);
        return ResponseEntityBuilder
                .getBuilder()
                .setDetails(postList)
                .build();
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
                                                   @RequestParam(value = "limit", defaultValue = "10") final Integer limit,
                                                   @RequestParam(value = "tags", required = false) final Set<PostTag> tags,
                                                   @RequestParam(value = "categoryId", required = false) final UUID categoryId,
                                                   @RequestParam(defaultValue = "createdDate") final String sortField,
                                                   @RequestParam(defaultValue = "DESC") final String sortDirection) {

        var filter = PostFilterRequest.builder()
                .categoryId(categoryId)
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
