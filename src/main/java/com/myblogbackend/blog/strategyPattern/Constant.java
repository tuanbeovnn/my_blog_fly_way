package com.myblogbackend.blog.strategyPattern;

public class Constant {
    private static final String DEV_PROFILE = "dev";
    private static final String PROD_PROFILE = "prod";

    public static String getDevProfile() {
        return DEV_PROFILE;
    }

    public static String getProdProfile() {
        return PROD_PROFILE;
    }
}