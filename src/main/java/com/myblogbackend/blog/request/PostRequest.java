package com.myblogbackend.blog.request;


import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;
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
    @NotBlank(message = "Category Id info cannot be blank")
    private UUID categoryId;
    @NotBlank(message = "Short Description info cannot be blank")
    private String shortDescription;
    private List<String> thumnails;
    private List<String> images;
}
