package io.github.jixingdefeng.visionrealm.common.util.file;

import net.minecraft.resources.ResourceLocation;

public class PathUtil {

    /**
     * Validates that the file path follows the resource naming convention:
     * <ul>
     *   <li>The first subfolder after base path must equal the resource's namespace</li>
     *   <li>The file name (without extension) must equal the resource's path</li>
     * </ul>
     * <p>
     * <b>Note: The base path should not end with a slash ("/").</b>
     *
     * @param resourceId The resource's location (namespace:path)
     * @param fileId     The file's resource location
     * @param basePath   The base path (without trailing slash, e.g., "erosion/block")
     * @return true if both conditions are satisfied
     */
    public static boolean isValidResourcePath(ResourceLocation resourceId, ResourceLocation fileId, String basePath) {
        String filePath = fileId.getPath();
        String fileName = extractFileName(filePath, false);
        String folderName = extractFirstSubfolder(filePath, basePath);
        return resourceId.getPath().equals(fileName) && resourceId.getNamespace().equals(folderName);
    }

    /**
     * Extracts the file name from a path string.
     *
     * @param basePath  The full file path (should not end with a slash "/")
     * @param keepExtension Whether to keep the file extension (e.g., ".json")
     * @return The file name (with extension if {@code keepExtension} is {@code true})
     */
    public static String extractFileName(String basePath, boolean keepExtension) {
        if (!keepExtension) {
            int index = basePath.contains(".") ? basePath.lastIndexOf(".") : basePath.length();
            basePath = basePath.substring(0, index);
        }

        return basePath.substring(basePath.lastIndexOf('/') + 1);
    }

    /**
     * Extracts the folder name from a path string relative to the base path.
     * <p>
     * For example, given base path "erosion/block/" and a file at
     * "data/erosion/block/minecraft/overworld/stone.json", this method returns "minecraft".
     * If the file is directly under the base path (no additional subfolder),
     * it returns an empty string.
     * <p>
     * <b>Note: The base path should not end with a slash ("/").</b>
     *
     * @param fullPath The full file path
     * @param basePath The base path to strip (without trailing slash)
     * @return The first subfolder name after the base path, or empty string if none
     */
    public static String extractFirstSubfolder(String fullPath, String basePath) {
        String subdirectories = extractSubdirectoryPath(fullPath, basePath);
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
     * Extracts the subdirectory part from a path string relative to the base path.
     * <p>
     * For example, given base path "erosion/block" and a file at
     * "data/erosion/block/minecraft/overworld/stone.json", this method returns "minecraft/overworld".
     * If the file is directly under the base path, returns an empty string.
     * <p>
     * <b>Note: The base path should not end with a slash ("/").</b>
     *
     * @param sourcePath The full file path
     * @param basePath   The base path to strip (without trailing slash)
     * @return The subdirectory path after the base path, or empty string if none
     */
    public static String extractSubdirectoryPath(String sourcePath, String basePath) {
        int index = sourcePath.indexOf(basePath);
        if (index >= 0) {
            int start = sourcePath.indexOf(basePath) + basePath.length() + 1;
            int end = sourcePath.lastIndexOf("/");
            if (end > start) {
                return sourcePath.substring(start, end);
            }
        }

        return "";
    }
}
