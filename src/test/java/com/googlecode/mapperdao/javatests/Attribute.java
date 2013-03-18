package com.googlecode.mapperdao.javatests;

/**
 * @author kostantinos.kougios
 *         <p/>
 *         3 Jul 2012
 */
public class Attribute {
    private String name, value;

    public Attribute(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        // simple impl
        if (obj instanceof Attribute) {
            Attribute a = (Attribute) obj;
            return name.equals(a.name) && value.equals(a.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        // simple impl
        return name.hashCode();
    }
}
