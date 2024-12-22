package helpers;

import com.google.gson.*;
import relation.Attribute;
import relation.Relation;
import relation.Row;
import relation.domaines.Domain;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class RelationTypeAdapter implements JsonDeserializer<Relation> {
    @Override
    public Relation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        // Deserialize the relation name and table name
        JsonElement nameElement = jsonObject.get("name");
        String name = (nameElement != null && !nameElement.isJsonNull()) ? nameElement.getAsString() : null;

        if (!jsonObject.has("tableName")) {
            throw new JsonParseException("Missing 'tableName' in the JSON object.");
        }
        String tableName = jsonObject.get("tableName").getAsString();

        // Deserialize the attributes and their domains
        JsonArray attributesArray = jsonObject.getAsJsonArray("attributes");
        if (attributesArray == null) {
            throw new JsonParseException("Missing or invalid 'attributes' in the JSON object.");
        }

        ArrayList<Attribute> attributes = new ArrayList<>();
        for (JsonElement element : attributesArray) {
            JsonObject attributeObject = element.getAsJsonObject();

            JsonObject domainObject = attributeObject.getAsJsonObject("domain");
            if (domainObject == null) {
                throw new JsonParseException("Missing 'domain' in the attribute.");
            }

            String domainType = domainObject.get("definition").toString();
            Domain domain = null;

// Check the class type of 'definition' field
            if (domainObject.get("definition").isJsonArray()) {
                // If definition is an array, we deserialize it into an Object array
                JsonArray definitionArray = domainObject.getAsJsonArray("definition");
                Object[] definition = context.deserialize(definitionArray, Object[].class);
                domain = new Domain(domainType, definition);
            } else {
                // If definition is not an array, handle it as a single object or primitive
                Object definition = context.deserialize(domainObject.get("definition"), Object.class);
                domain = new Domain(domainType, definition);
            }


            // Deserialize the attribute name
            String attributeName = attributeObject.get("attributeName").getAsString();
            // Create and add the Attribute object
            Attribute attribute = new Attribute(attributeName, domain);

            attributes.add(attribute);
        }

        // Deserialize the rows (using the RowTypeAdapter)
        JsonArray rowsArray = jsonObject.getAsJsonArray("rows");
        ArrayList<Row> rows = new ArrayList<>();
        if (rowsArray != null) {
            for (JsonElement element : rowsArray) {
                JsonArray rowArray = element.getAsJsonArray();
                Object[] rowValues = new Object[rowArray.size()];

                for (int i = 0; i < rowArray.size(); i++) {
                    rowValues[i] = context.deserialize(rowArray.get(i), Object.class);
                }
                rows.add(new Row(rowValues));
            }
        }

        // Create and return the Relation object
        Relation relation = new Relation(tableName);
        for (Attribute attribute : attributes) {
            relation.addAttribute(attribute);
        }
        for (Row row : rows) {
            relation.addRowWithoutCheck2(row);
            // Print the row values directly
        }
        return relation;
    }
}
