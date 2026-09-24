package com.tracefinder;

import java.util.regex.Pattern;


public final class FieldCleaner {

    private static final Pattern HIDDEN_CHARACTERS = Pattern.compile(
            "[\u200B\u200C\u200D\uFEFF\u2060\u00AD\u200E\u200F\u061C]"
    );

    public static String clean(String raw) {
        if (raw == null) {
            return null;
        }
        String withoutHiddenCharacters = HIDDEN_CHARACTERS.matcher(raw).replaceAll("");
        return withoutHiddenCharacters.trim();
    }

    private FieldCleaner() {
        // no instances; this is just a holder for the static method
    }
}
