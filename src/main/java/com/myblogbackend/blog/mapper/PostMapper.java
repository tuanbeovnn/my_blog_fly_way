package com.myblogbackend.blog.mapper;

import com.myblogbackend.blog.mapper.utils.MappingUtil;
import com.myblogbackend.blog.models.PostEntity;
import com.myblogbackend.blog.request.PostRequest;
import com.myblogbackend.blog.response.PostResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = MappingUtil.class)
public interface PostMapper {
    @Mapping(target = "images", source = "images", qualifiedByName = "mapImages")
    @Mapping(target = "thumbnails", source = "thumbnails", qualifiedByName = "mapImages")
    @Mapping(target = "tags", source = "tags", qualifiedByName = "mapPostTagsToTagEntities")
    PostEntity toPostEntity(PostRequest postRequest);

    //    @Mapping(target = "images", source = "images", qualifiedByName = "mapImagesInformation")
    @Mapping(target = "thumbnails", source = "thumbnails", qualifiedByName = "mapImagesInformation")
    @Mapping(target = "tags", source = "tags", qualifiedByName = "mapTagEntitiesToPostTags")
    @Mapping(target = "createdBy", source = "user")
    PostResponse toPostResponse(PostEntity postEntity);

    List<PostResponse> toListPostResponse(List<PostEntity> postEntityList);


}
