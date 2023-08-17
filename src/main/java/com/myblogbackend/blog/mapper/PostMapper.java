package com.myblogbackend.blog.mapper;

import com.myblogbackend.blog.models.PostEntity;
import com.myblogbackend.blog.request.PostRequest;
import com.myblogbackend.blog.response.PostResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PostMapper {
    PostEntity toPostEntity(PostRequest postRequest);

    PostResponse toPostResponse(PostEntity postEntity);

    List<PostResponse> toListPostResponse(List<PostEntity> postEntityList);

    PostResponse toPostResponseFromObject(Object object);

    List<PostResponse> toListPostResponseFromObject(List<Object> objectList);

}
