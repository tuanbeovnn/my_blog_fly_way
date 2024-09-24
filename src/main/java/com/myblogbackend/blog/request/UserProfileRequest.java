package com.myblogbackend.blog.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
