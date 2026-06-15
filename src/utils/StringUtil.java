package utils;

import java.util.Locale;

public final class StringUtil {
    private StringUtil() {

    }

    public static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
