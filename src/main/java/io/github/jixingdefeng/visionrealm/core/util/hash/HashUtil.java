package io.github.jixingdefeng.visionrealm.core.util.hash;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import java.util.*;

/**
 * Utility for generating deterministic, content-based identifiers via {@link Codec}.
 * <p>
 * This class serializes objects to JSON using a {@link Codec}, then computes
 * a hash from the resulting JSON structure. The hash can be used as a
 * content-based identifier for the object.
 * <p>
 * <b>Key characteristics:</b>
 * <ul>
 *   <li>Deterministic: the same object always produces the same result</li>
 *   <li>Content-based: derived from JSON structure, not from object identity or memory address</li>
 *   <li>Reproducible: consistent across different JVM instances and environments</li>
 *   <li>Order-insensitive: the hash is independent of key ordering in JSON objects</li>
 * </ul>
 *
 * @see Codec
 * @see JsonElement
 * @since 0.1.0
 */
public final class HashUtil {

    /**
     * Computes a hexadecimal hash string for the given object(s).
     *
     * @param <T>     the type of the objects
     * @param codec   the codec used to serialize objects to JSON
     * @param objects the objects to hash (null values are skipped)
     * @return a hexadecimal string representation of the hash
     */
    @SafeVarargs
    public static <T> String hashString(Codec<T> codec, T... objects) {
        long hash = hashOf(codec, objects);
        return Long.toHexString(hash);
    }

    /**
     * Computes a combined hash for one or more objects using the provided Codec.
     * <p>
     * Each object is serialized to JSON using the Codec, and the JSON hash is
     * calculated via {@link #hashOf(JsonElement)}. The final hash is the sum
     * of all individual hashes, making the result independent of object order.
     * <p>
     * This method is designed for scenarios where multiple objects together
     * represent a single entity for hashing purposes.
     *
     * @param <T>     the type of the objects
     * @param codec   the Codec used to serialize objects to JSON
     * @param objects the objects to hash (null values are skipped)
     * @return the combined hash value
     */
    @SafeVarargs
    public static <T> long hashOf(Codec<T> codec, T... objects) {
        long hash = 0;
        for (T object : objects) {
            if (object != null) {
                JsonElement jsonElement = codec.encodeStart(JsonOps.INSTANCE, object).getOrThrow();
                hash += hashOf(jsonElement);
            }
        }

        return hash;
    }

    /**
     * Computes a deterministic hash from a JSON element tree.
     * <p>
     * This method traverses the entire JSON structure (objects, arrays, primitives)
     * and produces a hash that is:
     * <ul>
     *   <li>Independent of key insertion order in {@link JsonObject}</li>
     *   <li>Deterministic across different JVM instances and environments</li>
     *   <li>Consistent for equivalent JSON structures</li>
     * </ul>
     * <p>
     * The algorithm recursively processes JSON elements using a stack, ensuring
     * that nested structures are handled without recursion depth limitations.
     *
     * @param jsonElement the JSON element to hash
     * @return a deterministic long hash value
     */
    public static long hashOf(JsonElement jsonElement) {
        long result = 1;
        Deque<JsonElement> stack = new ArrayDeque<>();
        stack.push(jsonElement);

        while (!stack.isEmpty()) {
            JsonElement element = stack.pop();
            if (element.isJsonNull()) {
                result = 31 * result;
            } else if (element.isJsonArray()) {
                JsonArray jsonArray = element.getAsJsonArray();
                int size = jsonArray.size();
                result = 31 * result + 4;
                result = 31 * result + size;
                for (int i = size - 1; i >= 0; i--) {
                    stack.push(jsonArray.get(i));
                }
            } else if (element.isJsonObject()) {
                JsonObject jsonObject = element.getAsJsonObject();
                result = 31 * result + 5;

                List<String> keys = new ArrayList<>(jsonObject.keySet());
                Collections.sort(keys);

                for (String key : keys) {
                    result = 31 * result + key.hashCode();
                }

                for (int i = keys.size() - 1; i >= 0; i--) {
                    stack.push(jsonObject.get(keys.get(i)));
                }
            } else if (jsonElement.isJsonPrimitive()) {
                JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
                if (jsonPrimitive.isBoolean()) {
                    result = 31 * result + 1;
                    result = 31 * result + Boolean.hashCode(jsonPrimitive.getAsBoolean());
                } else  if (jsonPrimitive.isNumber()) {
                    result = 31 * result + 2;
                    result = 31 * result + Double.hashCode(jsonPrimitive.getAsNumber().doubleValue());
                } else  if (jsonPrimitive.isString()) {
                    result = 31 * result + 3;
                    result = 31 * result + jsonPrimitive.getAsString().hashCode();
                }
            }
        }

        return result;
    }

    private HashUtil() {
    }
}
