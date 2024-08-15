package com.myblogbackend.blog.response;

import com.myblogbackend.blog.enums.PostTag;
import com.myblogbackend.blog.enums.RatingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
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
    private String slug;
    private List<String> thumbnails;
//    private List<String> images;
    private UserResponse createdBy;
    private List<UserLikedPostResponse> usersLikedPost = new ArrayList<>();
    private RatingType favoriteType;
    private Long favourite;
    private int totalComments;
    private CategoryResponse category;
    private Set<PostTag> tags = new HashSet<>();
    private Date createdDate;
}
