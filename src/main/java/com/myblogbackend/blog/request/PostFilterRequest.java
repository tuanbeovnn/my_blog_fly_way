package com.myblogbackend.blog.request;

import com.myblogbackend.blog.enums.PostTag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class PostFilterRequest {
    private Set<PostTag> tags;
    private UUID categoryId;
    private UUID userId;
    private String title;
    private String content;
    private String shortDescription;

    private String sortField = "createdDate"; // Default sort field
    private String sortDirection = "DESC"; // Default sort direction

    public PostFilterRequest() {
    }
}
