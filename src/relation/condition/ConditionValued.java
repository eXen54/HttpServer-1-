package relation.condition;

import relation.Relation;

import java.util.Arrays;

public class ConditionValued {
    private final String attributeName;
    private final Condition condition;
    private final Object valueSearched;
    private final String type;

    private static final String [] types = new String[] { "and", "or"};

    public ConditionValued(String attributeName, Condition condition, Object valueSearched, String type) {
        System.out.println("type"+type);
        System.out.println("Condition: " + attributeName + " " + condition + " " + valueSearched + ", type: " + type);
        if (Arrays.stream(types).noneMatch(t -> t.equals(type))) {
            throw new IllegalArgumentException("Type not in type");
        }

        this.attributeName = attributeName;
        this.condition = condition;
        this.valueSearched = valueSearched;
        this.type = type;
    }
    public boolean checkConditionValued (Relation relation, int indexRow) {
        int indexCol = relation.indexAttribute(this.attributeName);
        if (indexCol < 0) {
            System.out.println("Attribute " + attributeName + " not found in relation");
            return false;
        }
        Object obj1 = relation.rows.get(indexRow).getValue(indexCol);
        return condition.check(obj1, this.valueSearched);
    }
    public static boolean checkConditionValued (ConditionValued conditionValued, Relation relation, int indexRow, int indexCol) {
        return conditionValued.checkConditionValued(relation, indexRow);
    }

    public String getAttributeName() {
        return attributeName;
    }

    public Condition getCondition() {
        return condition;
    }

    public Object getValueSearched() {
        return valueSearched;
    }

    public String getType() {
        return type;
    }
}
