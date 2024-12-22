package helpers;

import com.google.gson.*;
import relation.Row;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class RowTypeAdapter implements JsonDeserializer<Row> {
    @Override
    public Row deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        // We expect an array of values in the row (not a JSON object)
        Row row = new Row();
        JsonArray jsonArray = json.getAsJsonArray();
        ArrayList<Object> values = new ArrayList<>();

        // Loop through the array and add each value to the row
        for (JsonElement element : jsonArray) {
            if (element.isJsonPrimitive()) {
                values.add(element.getAsJsonPrimitive());  // Primitive value (String, Number, etc.)
            } else if (element.isJsonObject()) {
                // If it's a JSON object, you can handle it here if needed
                values.add(element.getAsJsonObject());
            } else if (element.isJsonArray()) {
                // Handle if there's a nested array, though it's not expected in this case
                values.add(element.getAsJsonArray());
            }
        }

        row.setValues(values); // Set the values to the Row object
        return row;
    }
}

