package io.github.jixingdefeng.visionrealm.common.util.hash;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import java.util.*;

public final class FingerprintUtil {

    @SafeVarargs
    public static <T> String hashString(Codec<T> codec, T... objects) {
        long hash = hashOf(codec, objects);
        return Long.toHexString(hash);
    }

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

    private FingerprintUtil() {
    }
}
