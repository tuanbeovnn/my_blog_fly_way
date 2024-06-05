package com.myblogbackend.blog.enums;

public enum TopicType {
    COMMENT("COMMENT"),
    NEWPOST("NEWPOST"),
    LIKE("LIKE");

    private final String type;

    TopicType(final String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
