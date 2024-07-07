package com.myblogbackend.blog.mapper;

import com.myblogbackend.blog.enums.PostTag;
import com.myblogbackend.blog.models.PostEntity;
import com.myblogbackend.blog.models.TagEntity;
import com.myblogbackend.blog.request.PostRequest;
import com.myblogbackend.blog.response.PostResponse;
import com.myblogbackend.blog.utils.GsonUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface PostMapper {
    @Mapping(target = "images", source = "images", qualifiedByName = "mapImages")
    @Mapping(target = "thumnails", source = "thumnails", qualifiedByName = "mapImages")
    @Mapping(target = "tags", source = "tags", qualifiedByName = "mapPostTagsToTagEntities")
    PostEntity toPostEntity(PostRequest postRequest);

    @Mapping(target = "images", source = "images", qualifiedByName = "mapImagesInformation")
    @Mapping(target = "thumnails", source = "thumnails", qualifiedByName = "mapImagesInformation")
    @Mapping(target = "tags", source = "tags", qualifiedByName = "mapTagEntitiesToPostTags")
    @Mapping(target = "createdBy", source = "user")
    PostResponse toPostResponse(PostEntity postEntity);

    List<PostResponse> toListPostResponse(List<PostEntity> postEntityList);

    @Named("mapImages")
    default String mapImages(List<String> s3Ids) {
        return GsonUtils.arrayToString(s3Ids);
    }

    @Named("mapImagesInformation")
    default List<String> mapImagesInformation(String images) {
        return GsonUtils.stringToArray(images, String[].class);
    }

    // Custom mapping method to map Set<FoodTag> to Set<TagEntity>
    @Named("mapPostTagsToTagEntities")
    default Set<TagEntity> mapFoodTagsToTagEntities(Set<PostTag> foodTags) {
        return foodTags.stream()
                .map(foodTag -> {
                    TagEntity tagEntity = new TagEntity();
                    tagEntity.setName(foodTag);
                    return tagEntity;
                })
                .collect(Collectors.toSet());
    }

    // Custom mapping method to map Set<TagEntity> to Set<FoodTag>
    @Named("mapTagEntitiesToPostTags")
    default Set<PostTag> mapTagEntitiesToFoodTags(Set<TagEntity> tagEntities) {
        return tagEntities.stream()
                .map(TagEntity::getName)
                .collect(Collectors.toSet());
    }


}
