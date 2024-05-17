package com.myblogbackend.blog.enums;

import lombok.Getter;

@Getter
public enum PostTag {
    WEB("WEB"),
    INFORMATION("INFORMATION"),
    TECHNOLOGY("TECHNOLOGY"),
    MOBILE("MOBILE"),
    AI("AI");

    private final String type;

    PostTag(final String type) {
        this.type = type;
    }

    public static PostTag fromString(final String type) {
        for (PostTag tag : PostTag.values()) {
            if (tag.getType().equalsIgnoreCase(type)) {
                return tag;
            }
        }
        throw new IllegalArgumentException("No constant with type " + type + " found");
    }
}
