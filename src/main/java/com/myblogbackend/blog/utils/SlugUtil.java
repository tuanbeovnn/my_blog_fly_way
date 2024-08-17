package com.myblogbackend.blog.utils;

import java.text.Normalizer;
import java.util.UUID;
import java.util.regex.Pattern;

public class SlugUtil {

    public static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    public static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    public static String makeSlug(final String input) {
        var whitespace = WHITESPACE.matcher(input).replaceAll("-");
        var normalized = Normalizer.normalize(whitespace, Normalizer.Form.NFD);
        var slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase() + UUID.randomUUID();
    }

    public static String splitFromEmail(final String email) {
        var parts = email.split("@");
        var uuid = UUID.randomUUID().toString();
        var uuidSubstring = uuid.replace("-", "").substring(0, 8);
        return parts[0] + uuidSubstring;
    }
}
