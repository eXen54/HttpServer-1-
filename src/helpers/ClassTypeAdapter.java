package helpers;

import com.google.gson.*;

import java.lang.reflect.Type;

public class ClassTypeAdapter implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {

    @Override
    public JsonElement serialize(Class<?> src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.getName()); // Serialize as the class name (String)
    }

    @Override
    public Class<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            return Class.forName(json.getAsString()); // Deserialize from class name (String)
        } catch (ClassNotFoundException e) {
            throw new JsonParseException("Class not found: " + json.getAsString(), e);
        }
    }
}
