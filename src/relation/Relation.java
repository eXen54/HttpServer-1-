package relation;

import relation.condition.Condition;
import relation.condition.ConditionValued;
import relation.domaines.Domain;
import relation.domaines.Interval;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Relation {
    public ArrayList<Attribute> attributes = new ArrayList<Attribute>();
    private String nom;
    public ArrayList<Row> rows = new ArrayList<Row>();

    public Relation() {

    }





    public ArrayList<Attribute> getAttributes() {
        return attributes;
    }

    public String toJSON() {
        StringBuilder jsonRelation = new StringBuilder();

        jsonRelation.append("{");

        jsonRelation.append("\"tableName\": \"").append(this.nom).append("\",");

        jsonRelation.append("\"attributes\": [");
        for (int i = 0; i < attributes.size(); i++) {
            Attribute attribute = attributes.get(i);
            jsonRelation.append("{");
            jsonRelation.append("\"attributeName\": \"").append(attribute.getAttributeName()).append("\",");

            // Serialize domain as a proper JSON object
            if (attribute.getDomain() != null) {
                jsonRelation.append("\"domain\": {");
                jsonRelation.append("\"domainName\": \"").append(attribute.getDomain().getDomainName()).append("\",");

                // Serialize the definition
                jsonRelation.append("\"definition\": ");
                Object definition = attribute.getDomain().getDefinition();
                if (definition instanceof Interval) {
                    // Special case if definition is a single Interval object
                    Interval interval = (Interval) definition;

                    jsonRelation.append("[")
                            .append(interval.getMinVal()).append(", ")
                            .append(interval.getMaxVal()).append("]");
                }
                if (definition instanceof Object[]) {
                    // If definition is an array (String or other types), serialize it as a JSON array
                    jsonRelation.append("[");
                    Object[] definitionArray = (Object[]) definition;
                    for (int j = 0; j < definitionArray.length; j++) {
                        // Directly serialize the definition without converting class names
                        jsonRelation.append("\"").append(definitionArray[j].toString()).append("\"");

                        if (j < definitionArray.length - 1) {
                            jsonRelation.append(",");
                        }
                    }
                    jsonRelation.append("]");
                } else {
                    // Directly serialize the definition as a string or primitive value
                    jsonRelation.append("\"").append(definition.toString()).append("\"");
                }

                jsonRelation.append("}");
            }

            jsonRelation.append("}");
            if (i < attributes.size() - 1) {
                jsonRelation.append(",");
            }
        }
        jsonRelation.append("],");

        jsonRelation.append("\"rows\": [");
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            jsonRelation.append("[");
            for (int j = 0; j < row.getValues().size(); j++) {
                Object value = row.getValue(j);
                if (value == null) {
                    jsonRelation.append("null");
                } else if (value instanceof String) {
                    jsonRelation.append("\"").append(value).append("\"");
                } else {
                    jsonRelation.append(value);
                }
                if (j < row.getValues().size() - 1) {
                    jsonRelation.append(",");
                }
            }
            jsonRelation.append("]");
            if (i < rows.size() - 1) {
                jsonRelation.append(",");
            }
        }
        jsonRelation.append("]");

        jsonRelation.append("}");

        return jsonRelation.toString();
    }






    // Method to save the JSON to a file
    public static void saveToFile(String jsonContent) {
        try {
            // Ensure the response folder exists
            System.out.println("on save");
            File directory = new File("response");
            if (!directory.exists()) {
                directory.mkdir();
                System.out.println("directory created");
            }

            // Create or overwrite the 'response.json' file
            FileWriter writer = new FileWriter("C:\\Users\\itu\\Documents\\Projet\\reseaux\\relation\\response\\response.json");

            System.out.println(jsonContent);
            writer.write(jsonContent);
            System.out.println("file saved");

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String getCsv() {
        StringBuilder csv = new StringBuilder();

        boolean first = true;
        for (Attribute attr : attributes) {
            if (!first) {
                csv.append(";");
            }
            csv.append(attr.getAttributeName());
            first = false;
        }

        return csv.toString();
    }

    public void setRows(ArrayList<Row> rows) {
        this.rows = rows;
    }


    public Row[] getRows() {
        return this.rows.toArray(new Row[0]);
    }

    public void addAttribute(Attribute attribute) {
        for (Attribute att : this.attributes) {
            if (att.sameName(attribute.getAttributeName())) {
                System.out.println("Attribute already exists with same name");
                return;
            }
        }
        attributes.add(attribute);
    }

    public void addRow(Object... values) {

        if (checkData(values)) {
            Row a = new Row();
            for (Object value : values) {
                a.addValue(value);
            }
            this.rows.add(a);
        }
    }public void addRowWithoutCheck(Object... values) {
            Row a = new Row();
            for (Object value : values) {
                a.addValue(value);
            }
            this.rows.add(a);
    }
    public void addRow2(Row a) {
            this.rows.add(a);
    }
    public void addRowWithoutCheck2(Row a) {
        this.rows.add(a);
    }


    public void afficheRow() {
        final String HORIZONTAL_LINE = "+-----------------".repeat(attributes.size()) + "+";
        final int COLUMN_WIDTH = 15;

        System.out.println("\nRelation name: " + this.getNom());

        System.out.println(HORIZONTAL_LINE);

        System.out.print("|");
        for (Attribute t : attributes) {
            String columnName = t.getAttributeName();

            if (columnName.length() > COLUMN_WIDTH) {
                columnName = columnName.substring(0, COLUMN_WIDTH - 3) + "...";
            }
            System.out.printf(" %-" + COLUMN_WIDTH + "s |", columnName);
        }
        System.out.println();

        System.out.println(HORIZONTAL_LINE);

        if (this.rows.isEmpty()) {
            System.out
                    .println("|" + centerText("Empty set, no rows", (COLUMN_WIDTH + 2) * attributes.size() - 1) + "|");
            System.out.println(HORIZONTAL_LINE);
            System.out.println(rows.size() + " rows in set\n");
            return;
        }

        for (Row row : this.rows) {
            System.out.print("|");
            for (int i = 0; i < row.getValues().size(); i++) {
                String value = String.valueOf(row.getValue(i));

                if (value.equals("null")) {
                    value = "NULL";
                }

                if (value.length() > COLUMN_WIDTH) {
                    value = value.substring(0, COLUMN_WIDTH - 3) + "...";
                }
                System.out.printf(" %-" + COLUMN_WIDTH + "s |", value);
            }
            System.out.println();
        }

        System.out.println(HORIZONTAL_LINE);
        System.out.println(rows.size() + " rows in set\n");
    }

    private String centerText(String text, int width) {
        int padding = (width - text.length()) / 2;
        return String.format("%" + padding + "s%s%" + padding + "s", "", text, "");
    }

    public Relation(String nom) {
        this.nom = nom;
    }

    public void setAttributes(ArrayList<Attribute> attributes) {
        this.attributes = new ArrayList<>(attributes);
    }

    public void setRows() {
        ArrayList<Row> uniqueRows = new ArrayList<>();

        for (Row row : this.rows) {
            boolean isDuplicate = false;
            for (Row uniqueRow : uniqueRows) {
                if (row.getValues().equals(uniqueRow.getValues())) {
                    isDuplicate = true;
                    break;
                }
            }
            if (!isDuplicate) {
                uniqueRows.add(row);
            }
        }

        this.rows = uniqueRows;
    }

    public static Relation filtered(Relation relation, String attributeName, Object valueSearched, Condition condition) {
        if (condition == null) {
            condition = Condition.EQUAL;  // Default to equality condition if no condition is provided
        }

        int attributeIndex = relation.indexAttribute(attributeName);

        if (attributeIndex < 0) {
            System.out.println("Attribute not found");
            return null;
        }

        Relation resultRelation = new Relation("Filtered row in " + relation.getNom() + " attribute: " + attributeName + " value: " + valueSearched + " condition: " + condition.getType());
        resultRelation.setAttributes(relation.attributes);

        ArrayList<Row> filteredRows = new ArrayList<>();
        for (Row row : relation.getRows()) {
            // Use condition to check if row value matches the searched value
            if (condition.check(row.getValue(attributeIndex), valueSearched)) {
                filteredRows.add(row);
            }
        }

        if (filteredRows.isEmpty()) {
            System.out.println("No row found with this attribute and value");
        }

        resultRelation.setRows(filteredRows); // Set the filtered rows
        return resultRelation;
    }


    public static Relation filtered(Relation relation, ConditionValued... conditions) {
        if (conditions.length == 0) {
            throw new IllegalArgumentException(" No Conditions ");
        }
        Relation r = new Relation(" Filtered row  ");
        r.setAttributes(relation.attributes);

        ArrayList<ConditionValued> orConditions = new ArrayList<>();
        ArrayList<ConditionValued> andConditions = new ArrayList<>();

        for (ConditionValued condition : conditions) {
            if (condition.getType().equals("or")) {
                orConditions.add(condition);
            } else {
                andConditions.add(condition);
            }
        }

        ArrayList<Row> result = new ArrayList<>();
        Row[] allRows = relation.getRows();

        for (int i = 0; i < allRows.length; i++) {
            boolean orValid = orConditions.isEmpty();
            for (ConditionValued orCondition : orConditions) {
                if (orCondition.checkConditionValued(relation, i)) {
                    orValid = true;
                    break;
                }
            }

            if (orValid) {
                boolean andValid = true;
                for (ConditionValued andCondition : andConditions) {
                    if (!andCondition.checkConditionValued(relation, i)) {
                        andValid = false;
                        break;
                    }
                }
                if (andValid) {
                    result.add(allRows[i]);
                }
            }
        }

        r.rows = result;
        r.setRows();
        return r;
    }

    public static Relation joinCross(Relation relation1, Relation relation2, Condition joinCondition,
            String relation1Attribute, String relation2Attribute) {
        Relation result = crossJoin(relation1, relation2);
        if (joinCondition == null) {
            joinCondition = Condition.EQUAL;
        }

        int relation1Index = relation1.indexAttribute(relation1Attribute);
        int relation2Index = relation2.indexAttribute(relation2Attribute) + relation1.attributes.size();

        ArrayList<Row> rowResult = new ArrayList<Row>();

        if (relation1Index < 0 || relation2Index < 0) {
            System.out.println("Join attributes not found");
            return null;
        }
        for (Row row : result.rows) {
            if (joinCondition.check(row.getValue(relation1Index), row.getValue(relation2Index))) {
                rowResult.add(row);
            }
        }

        result.rows = rowResult;
        return result;
    }

    public static Relation joinCross(Relation relation1, Relation relation2, ConditionValued... conditions) {
        if (conditions.length == 0) {
            throw new IllegalArgumentException("No Conditions provided for join");
        }
        Relation result = crossJoin(relation1, relation2);
        return filtered(result, conditions);
    }

    public static Relation crossJoin(Relation relation1, Relation relation2) {
        Relation result = new Relation("Cross join between : " + relation1.getNom() + " and : " + relation2.getNom());

        result.attributes.addAll(relation1.attributes);
        result.attributes.addAll(relation2.attributes);

        for (Row relation1Row : relation1.rows) {
            for (Row relation2Row : relation2.rows) {

                Row joinedRow = new Row();
                joinedRow.addValues(relation1Row.getValues());
                joinedRow.addValues(relation2Row.getValues());
                result.rows.add(joinedRow);
            }
        }
        result.setRows();

        System.out.println("result:");
        result.afficheRow();
        return result;
    }

    public int getColumnIndex(String columnName) {
        for (int i = 0; i < attributes.size(); i++) {
            if (attributes.get(i).getAttributeName().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }
    public void clearRows() {
        this.rows.clear(); // Assuming `rows` is a List<Row>
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= rows.size()) {
            throw new IndexOutOfBoundsException("Row index out of bounds: " + rowIndex);
        }
        Row row = rows.get(rowIndex);
        return row.getValue(columnIndex);
    }


    public void deleteRows(Row[] filteredRows) {
        for (Row a : filteredRows) {
            this.rows.remove(a);
        }
    }

    public int indexAttribute(String attributeName) {
        for (int i = 0; i < this.attributes.size(); i++) {
            if (this.attributes.get(i).sameName(attributeName)) {
                return i;
            }
        }
        return -1;
    }

    public static Relation updateRows(Relation r, String attributeName, Object valueSearched, Object newValue, Condition condition) {
        int indexAttribute = r.indexAttribute(attributeName);

        // If no condition is provided, use equality by default
        if (condition == null) {
            condition = Condition.EQUAL;
        }

        for (Row updated : r.getRows()) {
            if (condition.check(updated.getValue(indexAttribute), valueSearched)) {
                updated.setValue(indexAttribute, newValue); // Update the row value
            }
        }
        return r;
    }


    public static boolean checkAttributes(Relation r, int[] indexes, String... attributeNames) {
        for (int i = 0; i < attributeNames.length; i++) {
            int index = r.indexAttribute(attributeNames[i]);
            if (index < 0)
                return false;
            indexes[i] = index;
        }
        return true;
    }

    public static Relation getSubRelation(Relation r, String... attributeNames) {
        int lenAtr = attributeNames.length;
        int[] indexes = new int[lenAtr];

        if (!checkAttributes(r, indexes, attributeNames)) {
            throw new IllegalArgumentException(
                    "Attributes error, " + Arrays.toString(attributeNames) + " is not found ");
        }

        Relation result = new Relation(
                " Sub relation  in " + r.getNom() + " attributes : " + Arrays.toString(attributeNames));

        for (int k : indexes) {
            result.attributes.add(r.attributes.get(k));
        }

        for (Row row : r.getRows()) {
            ArrayList<Object> values = new ArrayList<>();
            for (int i : indexes) {
                values.add(row.getValue(i));
            }
            result.addRow(values.toArray());
        }
        result.setRows();
        return result;
    }

    public static Relation deleteRows(Relation r, String attributeName, Object valueSearched, Condition condition) {
        int indexAttribute = r.indexAttribute(attributeName);
        ArrayList<Row> rowsToRemove = new ArrayList<>();

        // If no condition is provided, use equality by default
        if (condition == null) {
            condition = Condition.EQUAL;
        }

        for (Row row : r.getRows()) {
            if (condition.check(row.getValue(indexAttribute), valueSearched)) {
                rowsToRemove.add(row); // Add rows to be deleted
            }
        }

        // Remove rows
        r.rows.removeAll(rowsToRemove);
        return r;
    }


    private boolean checkData(Object[] values) {
        if (values.length != this.attributes.size()) {
            System.out.println("Length of attributes not enough");
            return false;
        }
        for (int i = 0; i < values.length; i++) {
            if (!this.attributes.get(i).checkValue(values[i])) {
                System.out.println("Wrong type value : " + values[i] + " for attribute "
                        + this.attributes.get(i).getAttributeName());
                return false;
            }
        }
        return true;
    }

    private static boolean checkData(Relation rel, Object[] values) {
        return rel.checkData(values);
    }

    public static void addAllValidRows(Relation mainRelation, Relation... others) {
        for (Relation relation : others) {
            for (Row row : relation.rows) {
                if (checkData(mainRelation, row.getValues().toArray())) {
                    mainRelation.rows.add(row);
                }
            }
        }
    }

    public static void mergeAttributesAndDomains(Relation result, String type, Relation... others) {
        if (others.length == 0) {
            throw new IllegalArgumentException("No relations provided for merging");
        }

        int size = others[0].attributes.size();
        Relation relation0 = others[0];
        Domain[] domainsForEachAttribute = new Domain[size];

        for (int i = 0; i < size; i++) {
            domainsForEachAttribute[i] = relation0.attributes.get(i).getDomain();
        }

        for (int i = 0; i < size; i++) {
            for (int k = 1; k < others.length; k++) {
                switch (type) {
                    case "union" ->
                        domainsForEachAttribute[i] = Domain.union(domainsForEachAttribute[i],
                                others[k].attributes.get(i).getDomain());
                    case "intersection" ->
                        domainsForEachAttribute[i] = Domain.intersection(domainsForEachAttribute[i],
                                others[k].attributes.get(i).getDomain());
                    case "difference" ->
                        domainsForEachAttribute[i] = Domain.difference(domainsForEachAttribute[i],
                                others[k].attributes.get(i).getDomain());
                }
            }
            result.attributes.add(new Attribute("Attribute " + i, domainsForEachAttribute[i]));
        }
    }

    public static Relation difference(Relation... others) {
        if (others.length < 2) {
            throw new IllegalArgumentException("Error found, attributes are < 2");
        }
        int size = others[0].attributes.size();
        for (Relation other : others) {
            if (other.attributes.size() != size) {
                throw new IllegalArgumentException(
                        "All relations must have the same number of attributes for merging.");
            }
        }
        Relation result = new Relation("Difference between relations ");
        String type = "difference";

        mergeAttributesAndDomains(result, type, others);

        addAllValidRows(result, others);

        result.setRows();
        return result;
    }

    public static Relation intersection(Relation... others) {
        if (others.length < 2) {
            throw new IllegalArgumentException("Error found, attributes are < 2");
        }
        int size = others[0].attributes.size();
        for (Relation other : others) {
            if (other.attributes.size() != size) {
                throw new IllegalArgumentException(
                        "All relations must have the same number of attributes for merging.");
            }
        }
        Relation result = new Relation("Intersection between relations ");
        String type = "intersection";

        mergeAttributesAndDomains(result, type, others);

        addAllValidRows(result, others);

        result.setRows();
        return result;
    }

    public static Relation union(Relation... others) {
        if (others.length < 2) {
            throw new IllegalArgumentException("Error found, attributes are < 2");
        }
        int size = others[0].attributes.size();
        for (Relation other : others) {
            if (other.attributes.size() != size) {
                throw new IllegalArgumentException(
                        "All relations must have the same number of attributes for merging.");
            }
        }
        Relation result = new Relation("Union between relations ");
        String type = "union";

        mergeAttributesAndDomains(result, type, others);

        addAllValidRows(result, others);

        result.setRows();
        return result;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }
}
