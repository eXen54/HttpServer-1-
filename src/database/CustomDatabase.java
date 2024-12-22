package database;

import relation.Relation;
import relation.Row;
import relation.Attribute;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CustomDatabase {
    private String nomDatabase;
    private ArrayList<Relation> relations;

    public ArrayList<Relation> getRelations() {
        return relations;
    }

    public void setRelations(ArrayList<Relation> relations) {
        this.relations = relations;
    }

    public CustomDatabase(String nomDatabase) {
        setNomDatabase(nomDatabase);
        this.relations = new ArrayList<>();
    }
    public CustomDatabase(String nomDatabase, Relation ... relations) {
        setNomDatabase(nomDatabase);
        this.relations = new ArrayList<>(List.of(relations));
    }

    public void printDatabase() {
        System.out.println("Database: " + nomDatabase);
        System.out.println("===================================");

        for (Relation relation : relations) {
            System.out.println("Table: " + relation.getNom());
            System.out.println("Attributes: ");

            for (Attribute attribute : relation.getAttributes()) {
                System.out.print(attribute.getAttributeName() + " ");
            }
            System.out.println("\n----------------------");

            System.out.println("Rows: ");
            for (Row row : relation.getRows()) {
                for (Object value : row.getValues()) {
                    System.out.print(value + "\t");
                }
                System.out.println();
            }
            System.out.println("===================================");
        }
    }

    public void addTable(Relation relation) {
        this.relations.add(relation);
    }

    public void saveDatabase() {
        // Create path to list directory
        String filePath = Paths.get("src", "database", "list", this.nomDatabase + ".json").toString();

        // Ensure directory exists
        new File(Paths.get("src", "database", "list").toString()).mkdirs();

        try (FileWriter writer = new FileWriter(filePath)) {
            // Start of JSON array
            writer.write("[\n");

            for (int i = 0; i < relations.size(); i++) {
                Relation relation = relations.get(i);
                for (int j = 0; j < relations.get(i).getAttributes().size(); j++) {
                    relations.get(i).getAttributes().get(j).getDomain().setDomainName("String");
                }
                writer.write(relation.toJSON());

                // Add a comma after each relation except the last one
                if (i < relations.size() - 1) {
                    writer.write(",");
                }
                writer.write("\n");
            }

            // End of JSON array
            writer.write("]");
        } catch (IOException e) {
            System.err.println("Error saving database: " + e.getMessage());
        }
        System.out.println("Database saved succesfully");
    }

    public static CustomDatabase loadDatabase(String nomDatabase) {
        CustomDatabase db = new CustomDatabase(nomDatabase);
        String filePath = Paths.get("src", "database", "list", nomDatabase + ".csv").toString();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            Relation currentRelation = null;
            boolean readingStructure = false;

            while ((line = reader.readLine()) != null) {
                if (line.equals("-/-")) {
                    if (currentRelation != null) {
                        db.addTable(currentRelation);
                    }
                    // Next line after -/- should be relation name
                    line = reader.readLine();
                    if (line != null && !line.equals("-/-")) {
                        currentRelation = new Relation(line);
                        readingStructure = true;
                    }
                    continue;
                }

                if (currentRelation != null) {
                    if (readingStructure) {
                        // Parse attribute names from CSV structure
                        String[] attributeNames = line.split(";");
                        for (String attrName : attributeNames) {
                            currentRelation.addAttribute(new Attribute(attrName.trim(), null));
                        }
                        readingStructure = false;
                    } else {
                        // Parse data row
                        String[] values = line.split(";");
                        ArrayList<Object> rowValues = new ArrayList<>();
                        for (String value : values) {
                            rowValues.add("NULL".equals(value) ? null : value);
                        }
                        currentRelation.addRow(rowValues.toArray());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading database: " + e.getMessage());
            return null;
        }
        return db;
    }
    public Relation getRelationByName(String relationName) {
        for (Relation relation : relations) {
            if (relation.getNom().equals(relationName)) {
                return relation;
            }
        }
        return null; // Return null if no relation is found by that name
    }


    public String getNomDatabase() {
        return nomDatabase;
    }

    public void setNomDatabase(String nomDatabase) {
        this.nomDatabase = nomDatabase;
    }
}