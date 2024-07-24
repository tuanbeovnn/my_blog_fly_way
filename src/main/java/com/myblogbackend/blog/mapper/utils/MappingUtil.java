package com.myblogbackend.blog.mapper.utils;

import com.myblogbackend.blog.enums.PostTag;
import com.myblogbackend.blog.models.TagEntity;
import com.myblogbackend.blog.utils.GsonUtils;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MappingUtil {

    @Named("mapImages")
    public static String mapImages(final List<String> s3Ids) {
        return GsonUtils.arrayToString(s3Ids);
    }

    @Named("mapImagesInformation")
    public static List<String> mapImagesInformation(final String images) {
        return GsonUtils.stringToArray(images, String[].class);
    }

    @Named("mapPostTagsToTagEntities")
    public static Set<TagEntity> mapFoodTagsToTagEntities(final Set<PostTag> foodTags) {
        return foodTags.stream()
                .map(foodTag -> {
                    TagEntity tagEntity = new TagEntity();
                    tagEntity.setName(foodTag);
                    return tagEntity;
                })
                .collect(Collectors.toSet());
    }

    @Named("mapTagEntitiesToPostTags")
    public static Set<PostTag> mapTagEntitiesToFoodTags(final Set<TagEntity> tagEntities) {
        return tagEntities.stream()
                .map(TagEntity::getName)
                .collect(Collectors.toSet());
    }

}
