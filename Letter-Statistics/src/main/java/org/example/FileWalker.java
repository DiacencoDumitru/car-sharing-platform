package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class FileWalker {

    private FileWalker() {}

    static List<Path> listFiles(Path root, Set<String> exts) throws IOException {
        try (Stream<Path> s = Files.walk(root)) {
            return s.filter(Files::isRegularFile)
                    .filter(p -> exts.isEmpty() || exts.contains(ext(p)))
                    .toList();
        }
    }

    public static String ext(Path p) {
        String n = String.valueOf(p.getFileName());
        int i = n.lastIndexOf('.');
        return i < 0 ? "" : n.substring(i + 1).toLowerCase(Locale.ROOT);
    }

    static Set<String> parseExts(String arg) {
        if (arg == null || arg.isBlank()) return defaultExts();
        return Arrays.stream(arg.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    static Set<String> defaultExts() {
        return Set.of("txt", "md", "java", "xml", "yml", "yaml", "properties", "csv", "json", "html");
    }
}
