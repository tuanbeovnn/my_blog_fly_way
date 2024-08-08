package com.myblogbackend.blog.enums;

public enum KafkaType {
    PRODUCER("PRODUCER"),
    CONSUMER("CONSUMER");

    private final String type;

    KafkaType(final String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
