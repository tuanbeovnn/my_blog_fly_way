package com.myblogbackend.blog.request;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class PostFilterRequest {
    private Set<String> tags;

    private String sortField = "createdDate"; // Default sort field
    private String sortDirection = "DESC"; // Default sort direction

    public PostFilterRequest(final Set<String> tags,
                             final String sortField, final String sortDirection) {

        this.tags = tags;
        this.sortField = sortField;
        this.sortDirection = sortDirection;
    }

}
