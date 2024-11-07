package com.myblogbackend.blog.controllers;

import com.myblogbackend.blog.controllers.route.CommonRoutes;
import com.myblogbackend.blog.request.PostFilterRequest;
import com.myblogbackend.blog.response.ResponseEntityBuilder;
import com.myblogbackend.blog.services.PostService;
import com.myblogbackend.blog.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(CommonRoutes.BASE_API + CommonRoutes.VERSION)
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    private final PostService postService;

    @GetMapping("/admin" + "/users-list")
    public ResponseEntity<?> getTopUsers(@RequestParam(value = "offset", defaultValue = "0") final Integer offset,
                                         @RequestParam(value = "limit", defaultValue = "9") final Integer limit) {
        var pageable = PageRequest.of(offset, limit);
        var usersList = userService.getListUsers(pageable);
        return ResponseEntityBuilder
                .getBuilder()
                .setDetails(usersList)
                .build();
    }


    @GetMapping("/admin" + "/articles-list")
    public ResponseEntity<?> getAllPostByFiltering(@RequestParam(value = "offset", defaultValue = "0") final Integer offset,
                                                   @RequestParam(value = "limit", defaultValue = "9") final Integer limit,
                                                   @RequestParam(defaultValue = "createdDate") final String sortField,
                                                   @RequestParam(defaultValue = "DESC") final String sortDirection) {

        var filter = PostFilterRequest.builder()
                .sortField(sortField)
                .sortDirection(sortDirection)
                .build();
        var pageable = PageRequest.of(offset, limit, Sort.Direction.fromString(sortDirection), sortField);

        var postFeeds = postService.getAllPostByStatus(pageable, filter);
        return ResponseEntityBuilder
                .getBuilder()
                .setDetails(postFeeds)
                .build();
    }

}