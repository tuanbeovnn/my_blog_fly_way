package com.myblogbackend.blog.controllers;

import com.myblogbackend.blog.controllers.route.CommentRoutes;
import com.myblogbackend.blog.controllers.route.CommonRoutes;
import com.myblogbackend.blog.request.CommentRequest;
import com.myblogbackend.blog.response.ResponseEntityBuilder;
import com.myblogbackend.blog.services.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(CommonRoutes.BASE_API)
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping(CommonRoutes.VERSION + CommentRoutes.BASE_URL)
    public ResponseEntity<?> createComment(@RequestBody final CommentRequest commentRequest) {
        var commentResponse = commentService.createComment(commentRequest);
        return ResponseEntity.ok(commentResponse);
    }

    @GetMapping("/v2" + "/public" + CommentRoutes.BASE_URL + "/{postId}")
    public ResponseEntity<?> retrieveCommentByPostIdV2(
            @RequestParam(name = "offset", defaultValue = "0") final int offset,
            @RequestParam(name = "limit", defaultValue = "10") final int limit,
            @PathVariable(value = "postId") final UUID postId) {

        var pageable = PageRequest.of(offset, limit, Sort.by(Sort.Order.desc("createdDate")));
        var commentResponseList = commentService.retrieveCommentByPostIdV2(pageable, postId);

        return ResponseEntityBuilder
                .getBuilder()
                .setDetails(commentResponseList)
                .build();
    }


    @GetMapping("/v2" + "/public" + CommentRoutes.BASE_URL + "/child/{parentId}")
    public ResponseEntity<?> retrieveChildCommentByPostIdV2(@PathVariable("parentId") final UUID parentId,
                                                            @RequestParam(name = "offset", defaultValue = "0") final Integer offset,
                                                            @RequestParam(name = "limit", defaultValue = "10") final Integer limit) {

        var pageable = PageRequest.of(offset, limit, Sort.by(Sort.Order.desc("createdDate")));
        var commentResponseList = commentService.retrieveCommentByPostIdV2(pageable, parentId);

        var response = commentService.retrieveChildCommentByParentId(parentId, pageable);
        return ResponseEntityBuilder.getBuilder()
                .setDetails(response)
                .build();
    }


    @PutMapping(CommonRoutes.VERSION + CommentRoutes.BASE_URL + "/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable final UUID commentId,
                                           @RequestBody @Valid final CommentRequest request) {
        var post = commentService.updateComment(commentId, request);
        return ResponseEntityBuilder.getBuilder()
                .setDetails(post)
                .build();
    }

    @PutMapping(CommonRoutes.VERSION + CommentRoutes.BASE_URL + "/disable/{commentId}")
    public ResponseEntity<?> disablePost(@PathVariable final UUID commentId) {
        commentService.disableComment(commentId);
        return ResponseEntityBuilder.getBuilder()
                .build();
    }
}
