package io.github.jixingdefeng.visionrealm.core.util.file;

import net.minecraft.resources.ResourceLocation;

public final class PathUtil {

    /**
     * Validates that the file path follows the resource naming convention:
     * <ul>
     *   <li>The first subfolder after base path must equal the resource's namespace</li>
     *   <li>The file name (without extension) must equal the resource's path</li>
     * </ul>
     * <p>
     * <b>Note: The base path should not end with a slash ("/").</b>
     *
     * <p><b>Behavior of {@code subPath} parameter:</b></p>
     * <ul>
     *   <li>{@code subPath = true}: Only the first subfolder after the base path is extracted.
     *       Example: {@code "erosion/block/minecraft/stone.json"} → {@code "minecraft"}</li>
     *   <li>{@code subPath = false}: The entire subdirectory path after the base path is extracted.
     *       Example: {@code "erosion/block/minecraft/overworld/stone.json"} → {@code "minecraft/overworld"}</li>
     * </ul>
     *
     * @param resourceId The resource's location (namespace:path)
     * @param fileId     The file's resource location
     * @param basePath   The base path (without trailing slash, e.g., {@code "erosion/block"})
     * @param subPath    Whether to match only the first subfolder ({@code true}) or the full subdirectory path ({@code false})
     * @return {@code true} if both conditions are satisfied
     */
    public static boolean isValidResourcePath(
            ResourceLocation resourceId,
            ResourceLocation fileId,
            String basePath,
            boolean subPath
    ) {
        String filePath = fileId.getPath();
        String fileName = extractFileName(filePath, false);
        String folderName = subPath
                            ? extractFirstSubfolder(filePath, basePath)
                            : extractSubdirectoryPath(filePath, basePath);
        return resourceId.getPath().equals(fileName) && resourceId.getNamespace().equals(folderName);
    }

    /**
     * Extracts the file name from a path string.
     * <p>
     * The file name is the part after the last directory separator ('/').
     * If {@code keepSuffix} is {@code true}, the file extension (e.g., ".json") is preserved;
     * otherwise, it is removed using {@link #removeSuffix(String)}.
     * <p>
     * Examples:
     * <ul>
     *   <li>{@code "path/to/file.json"} with {@code keepSuffix = true} → {@code "file.json"}</li>
     *   <li>{@code "path/to/file.json"} with {@code keepSuffix = false} → {@code "file"}</li>
     * </ul>
     *
     * @param basePath   The full file path (should not end with a slash)
     * @param keepSuffix Whether to keep the file extension
     * @return The file name (with extension if {@code keepSuffix} is {@code true})
     */
    public static String extractFileName(String basePath, boolean keepSuffix) {
        if (!keepSuffix) {
            basePath = removeSuffix(basePath);
        }

        return basePath.substring(basePath.lastIndexOf('/') + 1);
    }

    /**
     * Removes the file extension (suffix) from a path string.
     * <p>
     * This method removes everything after the last dot ('.') character,
     * provided that the dot appears after the last directory separator ('/').
     * This prevents accidentally removing dots that are part of directory names.
     * <p>
     * Examples:
     * <ul>
     *   <li>{@code "path/to/file.json"} → {@code "path/to/file"}</li>
     *   <li>{@code "path/to/file.tar.gz"} → {@code "path/to/file.tar"}</li>
     *   <li>{@code "path/to/folder.name/file.json"} → {@code "path/to/folder.name/file"}</li>
     *   <li>{@code "path/to/file"} → {@code "path/to/file"} (no change)</li>
     * </ul>
     *
     * @param path The path string (must not be {@code null})
     * @return The path without the suffix (or the original path if no suffix is found)
     */
    public static String removeSuffix(String path) {
        int lastSlash = path.lastIndexOf('/');
        int lastDot = path.lastIndexOf('.');
        if (lastDot > lastSlash) {
            return path.substring(0, lastDot);
        }

        return path;
    }

    /**
     * Removes a prefix from a path string, handling path separators correctly.
     * <p>
     * This method removes the prefix, accounting for the path separator {@code /}
     * so that the resulting string does not start with a slash.
     * <p>
     * Examples:
     * <ul>
     *   <li>{@code "erosion/block/minecraft/stone.json"} with prefix {@code "erosion/block"}
     *       → {@code "minecraft/stone.json"}</li>
     *   <li>{@code "erosion/block/minecraft/stone.json"} with prefix {@code "erosion/block/"}
     *       → {@code "minecraft/stone.json"}</li>
     * </ul>
     *
     * @param path   The full path string
     * @param prefix The prefix to remove (may or may not end with a slash)
     * @return The path with the prefix removed, without a leading slash
     */
    public static String removePrefix(String path, String prefix) {
        if (path.startsWith(prefix)) {
            int index = prefix.length() + (prefix.endsWith("/") ? 0 : 1);
            return path.substring(index);
        }

        return path;
    }

    /**
     * Extracts the first subfolder name from a path relative to the base path.
     * <p>
     * The base path may optionally end with a slash ({@code "/"}).
     * <p>
     * <b>Examples:</b>
     * <ul>
     *   <li>{@code "data/erosion/block/minecraft/overworld/stone.json"} with
     *       base path {@code "erosion/block"} → {@code "minecraft"}</li>
     *   <li>{@code "data/erosion/block/stone.json"} → {@code ""}</li>
     * </ul>
     *
     * @param sourcePath The full file path
     * @param basePath   The base path to strip (may or may not end with a slash)
     * @return The first subfolder name after the base path, or empty string if none
     */
    public static String extractFirstSubfolder(String sourcePath, String basePath) {
        String subdirectories = extractSubdirectoryPath(sourcePath, basePath);
        if (!subdirectories.isEmpty()) {
            int index = subdirectories.indexOf('/');
            if (index != -1) {
                return subdirectories.substring(0, index);
            } else {
                return subdirectories;
            }
        }

        return "";
    }

    /**
     * Extracts the subdirectory path from a file path relative to the base path.
     * <p>
     * The base path may optionally end with a slash ({@code "/"}).
     * <p>
     * <b>Examples:</b>
     * <ul>
     *   <li>{@code "data/erosion/block/minecraft/overworld/stone.json"} with
     *       base path {@code "erosion/block"} → {@code "minecraft/overworld"}</li>
     *   <li>{@code "data/erosion/block/stone.json"} → {@code ""}</li>
     * </ul>
     *
     * @param sourcePath The full file path
     * @param basePath   The base path to strip (may or may not end with a slash)
     * @return The subdirectory path after the base path, or empty string if none
     */
    public static String extractSubdirectoryPath(String sourcePath, String basePath) {
        int index = sourcePath.indexOf(basePath);
        if (index >= 0) {
            int start = index + basePath.length();
            if (!basePath.endsWith("/")) {
                start = start + 1;
            }

            int end = sourcePath.lastIndexOf("/");
            if (end > start) {
                return sourcePath.substring(start, end);
            }
        }

        return "";
    }

    private PathUtil() {
    }
}
