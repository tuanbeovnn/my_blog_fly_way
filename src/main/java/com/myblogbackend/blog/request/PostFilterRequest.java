package com.myblogbackend.blog.request;

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
    private Set<String> tags;
    private UUID categoryId;

    private String sortField = "createdDate"; // Default sort field
    private String sortDirection = "DESC"; // Default sort direction

    public PostFilterRequest() {
    }
}
