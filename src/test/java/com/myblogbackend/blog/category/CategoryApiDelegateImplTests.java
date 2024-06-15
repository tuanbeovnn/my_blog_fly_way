package com.myblogbackend.blog.category;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myblogbackend.blog.mapper.CategoryMapper;
import com.myblogbackend.blog.models.CategoryEntity;
import com.myblogbackend.blog.repositories.CategoryRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.myblogbackend.blog.category.CategoryTestApi.*;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser
public class CategoryApiDelegateImplTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryRepository categoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Test
    public void givenMoreComplexCategoryData_whenSendData_thenReturnsCategoryCreated() throws Exception {
        var categoryName = "Category A";
        var categoryRequest = prepareCategoryForRequesting();

        when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(makeCategoryForSaving(categoryName));
        var expectedCategoryResponse = categoryMapper.toCategoryResponse(makeCategoryForSaving(categoryName));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/category")
                        .content(objectMapper.writeValueAsString(categoryRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedCategoryResponse.getId().toString()))
                .andExpect(jsonPath("$.name").value(expectedCategoryResponse.getName()));
    }

    @Test
    public void givenUserRequestForListCategory_whenRequestCategoryList_thenReturnsCategoryList() throws Exception {
        var categoryEntities = prepareCategories();
        when(categoryRepository.findAllByStatusTrue(any(Pageable.class)))
                .thenReturn(new PageImpl<>(categoryEntities));

        var expectedCategoryList = categoryMapper.toListCategoryResponse(categoryEntities);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/public/category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details.totalRecords", is(2)))
                .andExpect(jsonPath("$.details.records[0].name", is(expectedCategoryList.get(0).getName())))
                .andExpect(jsonPath("$.details.records[1].name", is(expectedCategoryList.get(1).getName())));

    }

}
