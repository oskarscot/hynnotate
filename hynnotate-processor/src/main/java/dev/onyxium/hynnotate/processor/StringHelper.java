package dev.onyxium.hynnotate.processor;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class StringHelper {

    private StringHelper() {}

    public static String toPascalCase(String value) {
        return Arrays.stream(value.split("[\\W_]+"))
                .filter(s -> !s.isEmpty())
                .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1))
                .collect(Collectors.joining());
    }
}
