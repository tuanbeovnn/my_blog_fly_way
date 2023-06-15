package com.myblogbackend.blog.Testing;

import com.github.javafaker.Faker;
import com.myblogbackend.blog.models.CategoryEntity;
import com.myblogbackend.blog.repositories.CategoryRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TestingSavingData {

    private final CategoryRepository categoryRepository;

    @PostConstruct
    public void initData() {
        dataTesting();
    }

    @Transactional
    public void dataTesting() {
        removeData();
        insertData();
    }

    public void removeData() {
        categoryRepository.deleteAllInBatch();
    }

    public void insertData() {
        Faker faker = new Faker();
        for (int i = 0; i < 5; i++) {
            CategoryEntity category = CategoryEntity.builder()
                    .name("11")
                    .build();
            categoryRepository.save(category);
        }
    }

}
