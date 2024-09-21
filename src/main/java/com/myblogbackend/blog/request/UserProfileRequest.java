package com.myblogbackend.blog.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = false)
public class UserProfileRequest {
    private String bio;
    private String website;
    private String location;
    private String avatarUrl;
    private String twitter;
    private String linkedin;
    private String github;
    private String name;
}
