package com.myblogbackend.blog.enums;

public enum FavoriteObjectType {
    POST("POST"),
    COMMENT("COMMENT");
    private final String objectType;
    FavoriteObjectType( String objectType) {
        this.objectType = objectType;
    }

    public String getObjectType() {
        return objectType;
    }
    public static  FavoriteObjectType fromString(String objectType) {
        for (FavoriteObjectType favoriteObjectType : FavoriteObjectType.values()) {
            if(favoriteObjectType.getObjectType().equalsIgnoreCase(objectType)) {
                return favoriteObjectType;
            }
        }
        return POST;
    }
}
