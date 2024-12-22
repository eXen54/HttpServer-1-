package helpers;

import com.google.gson.*;
import relation.domaines.Domain;
import relation.domaines.Interval;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DomainTypeAdapter implements JsonDeserializer<Domain> {

    @Override
    public Domain deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        // Deserialize the domain name
        String domainName = jsonObject.get("domainName").getAsString();

        // Deserialize the definition array, which can contain various types
        JsonArray definitionArray = jsonObject.getAsJsonArray("definition");
        List<Object> definitionList = new ArrayList<>();

        for (JsonElement element : definitionArray) {
            if (element.isJsonPrimitive()) {
                // If the element is a primitive (String, Number, etc.), add it directly
                definitionList.add(element.getAsJsonPrimitive());
            } else if (element.isJsonObject()) {
                // If the element is an object, check if it's an Interval
                JsonObject obj = element.getAsJsonObject();
                if (obj.has("start") && obj.has("end")) {
                    // Handle the case where the object is an Interval
                    Interval interval = context.deserialize(obj, Interval.class);
                    definitionList.add(interval);
                } else {
                    // Otherwise, handle it as a general object (e.g., Class or another object)
                    definitionList.add(context.deserialize(element, Object.class));
                }
            } else {
                // For other types, handle them appropriately (e.g., Class type or simple object)
                definitionList.add(context.deserialize(element, Object.class));
            }
        }

        // Convert List<Object> to Object[] to match the Domain constructor
        Object[] definitionArrayResult = definitionList.toArray();

        // Create and return a new Domain object
        return new Domain(domainName, definitionArrayResult);
    }
}
