package com.myblogbackend.blog.request;


import com.myblogbackend.blog.enums.PostTag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostRequest {
    @NotBlank(message = "Title info cannot be blank")
    private String title;
    private String content;
    private UUID categoryId;
    @NotBlank(message = "Short Description info cannot be blank")
    private String shortDescription;
    @NotEmpty(message = "At least one valid is required.")
    private List<String> thumbnails;
    @NotEmpty(message = "At least one valid food tag is required.")
    private Set<PostTag> tags;
}
