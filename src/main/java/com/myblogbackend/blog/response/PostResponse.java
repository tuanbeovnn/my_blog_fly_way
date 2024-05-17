package com.myblogbackend.blog.response;

import com.myblogbackend.blog.enums.PostTag;
import com.myblogbackend.blog.enums.RatingType;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private String shortDescription;
    private List<String> thumnails;
    private List<String> images;
    private String createdBy;
    private List<UserLikedPostResponse> usersLikedPost = new ArrayList<>();
    private RatingType favoriteType;
    private CategoryResponse category;
    private Set<PostTag> tags = new HashSet<>();
}
