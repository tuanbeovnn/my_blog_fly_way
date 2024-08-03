package com.myblogbackend.blog.controllers;

import com.myblogbackend.blog.controllers.route.CommonRoutes;
import com.myblogbackend.blog.controllers.route.ViewerRoutes;
import com.myblogbackend.blog.response.ResponseEntityBuilder;
import com.myblogbackend.blog.services.ViewerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(CommonRoutes.BASE_API + CommonRoutes.VERSION + ViewerRoutes.BASE_URL)
@RequiredArgsConstructor
public class ViewerController {

    private final ViewerService viewerService;

    @PostMapping("/{postId}")
    public ResponseEntity<?> upgradeViewerByPostId(@PathVariable(value = "postId") final UUID postId) {
        viewerService.countUserViewer(postId);
        return ResponseEntityBuilder
                .getBuilder()
                .setMessage("Success")
                .build();
    }
}
