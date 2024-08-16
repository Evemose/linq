package org.linq.core;

public class Dummy {

    public String name;

    @Override
    public String toString() {
        return "Dummy{" +
            "name='" + name + '\'' +
            '}';
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
