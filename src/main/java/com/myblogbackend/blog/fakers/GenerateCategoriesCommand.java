package com.myblogbackend.blog.fakers;

import com.myblogbackend.blog.repositories.CategoryRepository;
import com.myblogbackend.blog.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@RequiredArgsConstructor
public class GenerateCategoriesCommand implements CommandLineRunner {

    private final CategoryService categoryService;
    private final Environment environment;
    private final CategoryRepository categoryRepository;

    @Override
    public void run(final String... args) throws Exception {
//        boolean shouldGenerate = environment.getProperty("app.generate-categories", Boolean.class, true);
//        if (!shouldGenerate) {
//            return;
//        }
//        Faker faker = new Faker();
//        var categoryList = categoryRepository.findAll();
//        if (categoryList.size() < 5) {
//            for (int i = 0; i < 10; i++) {
//                CategoryRequest categoryRequest = CategoryRequest.builder()
//                        .name(faker.commerce().productName())
//                        .build();
//                categoryService.createCategory(categoryRequest);
//            }
//        }
    }
}