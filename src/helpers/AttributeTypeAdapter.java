package helpers;

import com.google.gson.*;
import relation.Attribute;
import relation.domaines.Domain;

import java.lang.reflect.Type;

public class AttributeTypeAdapter implements JsonDeserializer<Attribute> {
    @Override
    public Attribute deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        // We expect the JSON to be an object that has "name", "type", and "domain"
        JsonObject jsonObject = json.getAsJsonObject();

        // Deserialize the name and type directly from the JSON
        String name = jsonObject.get("name").getAsString();

        // Deserialize the domain (assuming it's a nested JSON object)
        JsonObject domainObject = jsonObject.getAsJsonObject("domain");
        Domain domain = context.deserialize(domainObject, Domain.class); // Deserialize the domain

        // Create and return the Attribute object with the domain
        return new Attribute(name,  domain);
    }
}
