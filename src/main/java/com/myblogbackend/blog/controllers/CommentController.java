package com.myblogbackend.blog.controllers;

import com.myblogbackend.blog.controllers.route.CommentRoutes;
import com.myblogbackend.blog.controllers.route.CommonRoutes;
import com.myblogbackend.blog.request.CommentRequest;
import com.myblogbackend.blog.response.ResponseEntityBuilder;
import com.myblogbackend.blog.services.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(CommonRoutes.BASE_API + CommonRoutes.VERSION)
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<?> createComment(@RequestBody final CommentRequest commentRequest) {
        var commentResponse = commentService.createComment(commentRequest);
        return ResponseEntity.ok(commentResponse);
    }

    @GetMapping("/public" + CommentRoutes.BASE_URL + "/{postId}")
    public ResponseEntity<?> getListCommentsByPostId(
            @RequestParam(name = "offset", defaultValue = "0") final Integer offset,
            @RequestParam(name = "limit", defaultValue = "10") final Integer limit,
            @PathVariable(value = "postId") final UUID postId) {
        var commentResponseList = commentService.getListCommentsByPostId(offset, limit, postId);
        return ResponseEntityBuilder
                .getBuilder()
                .setDetails(commentResponseList)
                .build();

    }

    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable final UUID commentId,
                                           @RequestBody @Valid final CommentRequest request) {
        var post = commentService.updateComment(commentId, request);
        return ResponseEntityBuilder.getBuilder()
                .setDetails(post)
                .build();
    }

    @PutMapping("/disable/{commentId}")
    public ResponseEntity<?> disablePost(@PathVariable final UUID commentId) {
        commentService.disableComment(commentId);
        return ResponseEntityBuilder.getBuilder()
                .build();
    }
}
