package com.myblogbackend.blog.enums;

import lombok.Getter;

@Getter
public enum PostTag {
    WEB("WEB"),
    MOBILE("MOBILE"),
    AI("AI"),
    FRONTEND("FRONTEND"),
    BACKEND("BACKEND"),
    DEVOPS("DEVOPS"),
    CLOUD("CLOUD"),
    DATABASE("DATABASE"),
    SECURITY("SECURITY"),
    JAVA("JAVA"),
    SPRING("SPRING"),
    HIBERNATE("HIBERNATE"),
    DOCKER("DOCKER"),
    KUBERNETES("KUBERNETES"),
    JENKINS("JENKINS"),
    GIT("GIT"),
    CI_CD("CI/CD"),
    JAVASCRIPT("JAVASCRIPT"),
    NODEJS("NODEJS"),
    REACT("REACT"),
    ANGULAR("ANGULAR"),
    TYPESCRIPT("TYPESCRIPT"),
    HTML("HTML"),
    CSS("CSS"),
    SASS("SASS"),
    MYSQL("MYSQL"),
    REDIS("REDIS"),
    APACHE("APACHE"),
    MICROSERVICES("MICROSERVICES");

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
