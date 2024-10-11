package com.myblogbackend.blog.enums;

public enum RatingType {
    LIKE("LIKE"),
    UNLIKE("UNLIKE"),
    DISLIKE("DISLIKE");

    private final String type;

    RatingType(final String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static RatingType fromString(final String type) {
        for (RatingType ratingType : RatingType.values()) {
            if (ratingType.getType().equalsIgnoreCase(type)) {
                return ratingType;
            }
        }
        return UNLIKE; // Default value if no match is found
    }

}
