package com.myblogbackend.blog.dtos;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostElasticRequest implements Serializable {
    private UUID id;
    private String title;
    private String content;
    private String shortDescription;
}
