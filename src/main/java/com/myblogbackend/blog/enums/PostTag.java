package com.myblogbackend.blog.enums;

import lombok.Getter;

@Getter
public enum PostTag {
    WEB("WEB"),
    INFORMATION("INFORMATION"),
    TECHNOLOGY("TECHNOLOGY"),
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
    MAVEN("MAVEN"),
    GRADLE("GRADLE"),
    DOCKER("DOCKER"),
    KUBERNETES("KUBERNETES"),
    JENKINS("JENKINS"),
    GIT("GIT"),
    CI_CD("CI/CD"),
    JAVASCRIPT("JAVASCRIPT"),
    NODEJS("NODEJS"),
    REACT("REACT"),
    ANGULAR("ANGULAR"),
    VUE("VUE"),
    EXPRESS("EXPRESS"),
    TYPESCRIPT("TYPESCRIPT"),
    HTML("HTML"),
    CSS("CSS"),
    SASS("SASS"),
    LESS("LESS"),
    WEBPACK("WEBPACK"),
    NPM("NPM"),
    YARN("YARN"),
    PYTHON("PYTHON"),
    DJANGO("DJANGO"),
    FLASK("FLASK"),
    RUBY("RUBY"),
    RAILS("RAILS"),
    PHP("PHP"),
    LARAVEL("LARAVEL"),
    MYSQL("MYSQL"),
    POSTGRESQL("POSTGRESQL"),
    MONGODB("MONGODB"),
    REDIS("REDIS"),
    APACHE("APACHE"),
    NGINX("NGINX"),
    REST_API("REST API"),
    GRAPHQL("GRAPHQL"),
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
