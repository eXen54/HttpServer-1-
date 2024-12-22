package relation;

import java.util.ArrayList;
import java.util.Arrays;

public class Row {
    private ArrayList<Object> values = new ArrayList<Object>();
    public ArrayList<Object> getValues () {
        return this.values;
    }
    public void addValue (Object value) {
        this.values.add(value);
    }
    public Object getValue (int index) {
        return this.values.get(index);
    }
    public void setValue (int index ,Object value) {
        this.values.set(index, value);
    }
    public Row () {
    }

    public Row(Object[] rowValues) {
        if (rowValues != null) {
            this.values = new ArrayList<>(Arrays.asList(rowValues));
        }
    }

    public void setValues(ArrayList<Object> values) {
        this.values = values;
    }

    public void addValues(ArrayList<Object> values) {
        this.values.addAll(values);
    }
    @Override
    public String toString() {
        return values.toString();
    }

}
