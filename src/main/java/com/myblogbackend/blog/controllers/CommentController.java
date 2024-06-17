package com.myblogbackend.blog.controllers;

import com.myblogbackend.blog.controllers.route.CommentRoutes;
import com.myblogbackend.blog.controllers.route.CommonRoutes;
import com.myblogbackend.blog.request.CommentRequest;
import com.myblogbackend.blog.services.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(CommonRoutes.BASE_API + CommonRoutes.VERSION + CommentRoutes.BASE_URL)
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<?> createComment(@RequestBody final CommentRequest commentRequest) {
        var commentResponse = commentService.createComment(commentRequest);
        return ResponseEntity.ok(commentResponse);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getListCommentsByPostId(
            @RequestParam(name = "offset", defaultValue = "0") final Integer offset,
            @RequestParam(name = "limit", defaultValue = "10") final Integer limit,
            @PathVariable(value = "postId") final UUID postId) {
        var commentResponseList = commentService.getListCommentsByPostId(offset, limit, postId);
        return ResponseEntity.ok(commentResponseList);
    }
}
