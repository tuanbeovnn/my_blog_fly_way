package com.myblogbackend.blog.enums;

public enum FollowType {
    FOLLOW("FOLLOW"),
    UNFOLLOW("UNFOLLOW");

    private final String type;

    FollowType(final String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
