package com.myblogbackend.blog.response;

import com.myblogbackend.blog.enums.RatingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserLikedPostResponse {
    private UUID id;
    private String name;
    private RatingType type;
}
