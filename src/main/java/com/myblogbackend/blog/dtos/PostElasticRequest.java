package com.myblogbackend.blog.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostElasticRequest implements Serializable {
    private UUID id;
    private String title;
    private String content;
    private String shortDescription;

    @Override
    public String toString() {
        return "PostElasticRequest{" +
                "content='" + content + '\'' +
                ", id=" + id +
                ", title='" + title + '\'' +
                ", shortDescription='" + shortDescription + '\'' +
                '}';
    }
}
