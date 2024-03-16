package com.myblogbackend.blog.request;


import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostRequest {
    private String title;
    private String content;
    private UUID categoryId;
    private String shortDescription;
    private List<String> thumnails;
    private List<String> images;
}
