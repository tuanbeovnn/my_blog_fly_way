package com.myblogbackend.blog.controllers;

import com.myblogbackend.blog.controllers.route.CategoryRoutes;
import com.myblogbackend.blog.controllers.route.CommonRoutes;
import com.myblogbackend.blog.request.CategoryRequest;
import com.myblogbackend.blog.response.ResponseEntityBuilder;
import com.myblogbackend.blog.services.CategoryService;
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

import java.util.UUID;

import static com.myblogbackend.blog.controllers.route.PostRoutes.PUBLIC_URL;

@RestController
@RequestMapping(CommonRoutes.BASE_API + CommonRoutes.VERSION)
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping(PUBLIC_URL + CategoryRoutes.BASE_URL)
    public ResponseEntity<?> getAllCategories(@RequestParam(name = "offset", defaultValue = "0") final Integer offset,
                                              @RequestParam(name = "limit", defaultValue = "10") final Integer limit) {
        var categoryList = categoryService.getAllCategories(offset, limit);
        return ResponseEntityBuilder
                .getBuilder()
                .setDetails(categoryList)
                .build();
    }

    @PostMapping(CategoryRoutes.BASE_URL)
    public ResponseEntity<?> createCategory(@RequestBody final CategoryRequest categoryRequest) {
        var categoryResponse = categoryService.createCategory(categoryRequest);
        return ResponseEntity.ok(categoryResponse);
    }

    @PutMapping(CategoryRoutes.BASE_URL + "/{id}")
    public ResponseEntity<?> updateCategory(final CategoryRequest categoryRequest, @PathVariable(value = "id") final UUID id) {
        var category = categoryService.updateCategory(id, categoryRequest);
        return ResponseEntity.ok(category);
    }

    @PutMapping(CategoryRoutes.BASE_URL + "/status/{id}/{status}")
    public ResponseEntity<?> updateCategoryStatus(@PathVariable(value = "id") final UUID id, @PathVariable(value = "status") final Boolean status) {
        categoryService.updateCategoryStatus(id, status);
        return ResponseEntity.ok("Update category status successfully");
    }
}
