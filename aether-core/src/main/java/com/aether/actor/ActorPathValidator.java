package com.aether.actor;

import java.util.regex.Pattern;

/**
 * Utility class for validating actor paths.
 * <p>
 * Actor paths follow a hierarchical structure similar to file paths:
 * <ul>
 *   <li>Must start with a forward slash (/)</li>
 *   <li>Can contain alphanumeric characters, hyphens, and underscores</li>
 *   <li>Segments are separated by forward slashes</li>
 *   <li>Cannot contain consecutive slashes</li>
 *   <li>Cannot end with a slash (except for root "/")</li>
 * </ul>
 */
public final class ActorPathValidator {

    private static final Pattern PATH_PATTERN = Pattern.compile("^/[a-zA-Z0-9_-]+(/[a-zA-Z0-9_-]+)*$");
    private static final int MAX_PATH_LENGTH = 255;
    private static final int MAX_SEGMENTS = 10;

    private ActorPathValidator() {
        // Utility class, prevent instantiation
    }

    /**
     * Validates an actor path.
     *
     * @param path the path to validate
     * @throws IllegalArgumentException if the path is invalid
     */
    public static void validate(String path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Actor path cannot be null or empty");
        }

        if (path.length() > MAX_PATH_LENGTH) {
            throw new IllegalArgumentException(
                "Actor path exceeds maximum length of " + MAX_PATH_LENGTH + " characters: " + path);
        }

        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("Actor path must start with '/': " + path);
        }

        // Root path is valid
        if (path.equals("/")) {
            return;
        }

        if (!PATH_PATTERN.matcher(path).matches()) {
            throw new IllegalArgumentException(
                "Actor path contains invalid characters or format: " + path +
                " (allowed: alphanumeric, hyphens, underscores; segments separated by '/')");
        }

        String[] segments = path.split("/");
        if (segments.length > MAX_SEGMENTS + 1) { // +1 because split on "/" creates empty first element
            throw new IllegalArgumentException(
                "Actor path exceeds maximum depth of " + MAX_SEGMENTS + " segments: " + path);
        }
    }

    /**
     * Checks if a path is valid without throwing an exception.
     *
     * @param path the path to check
     * @return true if valid, false otherwise
     */
    public static boolean isValid(String path) {
        try {
            validate(path);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Extracts the parent path from a given path.
     * For example, "/user/actor" returns "/user".
     *
     * @param path the path
     * @return the parent path, or "/" if the path is at the root level
     */
    public static String getParentPath(String path) {
        validate(path);

        if (path.equals("/")) {
            return null;
        }

        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex <= 0) {
            return "/";
        }

        return path.substring(0, lastSlashIndex);
    }

    /**
     * Extracts the last segment (name) from a path.
     * For example, "/user/actor" returns "actor".
     *
     * @param path the path
     * @return the last segment
     */
    public static String getName(String path) {
        validate(path);

        if (path.equals("/")) {
            return "/";
        }

        int lastSlashIndex = path.lastIndexOf('/');
        return path.substring(lastSlashIndex + 1);
    }
}
