package com.myblogbackend.blog.enums;

public enum PostType {
    APPROVED("APPROVED"),
    REJECTED("REJECTED"),
    PENDING("PENDING");

    private final String type;

    PostType(final String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
