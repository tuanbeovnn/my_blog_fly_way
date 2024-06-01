package com.myblogbackend.blog.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Getter
public enum OAuth2Provider {
    GOOGLE("google"),

    LOCAL("local"),

    GITHUB("github");

    private final String provider;

    private static final Map<String, OAuth2Provider> PROVIDERS;

    static {
        PROVIDERS = new HashMap<>();
        for (OAuth2Provider provider : OAuth2Provider.values()) {
            PROVIDERS.put(provider.getProvider(), provider);
        }
    }

}