package com.myblogbackend.blog.response;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostResponse {
    private UUID id;
    private String title;
    private String content;
    private UUID categoryId;
    private String shortDescription;
    private List<String> thumnails;
    private List<String> images;
}
