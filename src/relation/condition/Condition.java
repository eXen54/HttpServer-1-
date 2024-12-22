package relation.condition;

import relation.domaines.Interval;

public class Condition {


    public String type;

    public static Condition EQUAL = new Condition("=");
    public static Condition DIFF = new Condition("!=");
    public static Condition SUP = new Condition(">");
    public static Condition INF = new Condition("<");
    public static Condition SUP_EQUAL = new Condition(">=");
    public static Condition INF_EQUAL = new Condition("<=");
    public static Condition LIKE = new Condition("like");


    public Condition(String type) {
        this.setType(type);
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean check(Object arg1, Object arg2) {
        if (arg1 == null || arg2 == null){
            return false;
        }
        if (arg1 instanceof Interval && arg2 instanceof Number) {
            Interval interval = (Interval) arg1;
            double value = ((Number) arg2).doubleValue();
            return interval.isInInterval(value);  // Check if the value is within the interval
        }
        System.out.println("arg1:"+arg1.toString());
        System.out.println("arg2:"+arg2.toString());
        if (this.type.equals("=")) {
            return arg1.equals(arg2) || arg1.toString().equals(arg2.toString());
        }
        if (this.type.equals("!=")) {
            return arg1 != arg2 || arg1.toString().equals(arg2.toString());
        }
        if (this.type.equals("like")) {
            if (arg1 instanceof String str1 && arg2 instanceof String str2) {
                return str2.startsWith(str1) || str1.startsWith(str2);
            }
            return false;
        }
        double arg1Double = Double.parseDouble(arg1.toString()), arg2Double = Double.parseDouble(arg2.toString());
        return switch (type) {
            case "<=" -> arg1Double < arg2Double || arg1.toString().equals(arg2.toString());
            case ">=" -> arg1Double > arg2Double || arg1.toString().equals(arg2.toString());
            case "<" -> arg1Double < arg2Double;
            case ">" -> arg1Double > arg2Double;
            default -> false;
        };
    }

    public static String[] parseCondition(String condition) {
        return condition.split("\\s+");
    }

    public static Condition mapOperatorToCondition(String operator) {
        switch (operator) {
            case "=":
                return Condition.EQUAL;
            case "!=":
                return Condition.DIFF;
            case "<":
                return Condition.INF;
            case ">":
                return Condition.SUP;
            case "<=":
                return Condition.INF_EQUAL;
            case ">=":
                return Condition.SUP_EQUAL;
            default:
                throw new IllegalArgumentException("Unknown condition operator: " + operator);
        }
    }

}
