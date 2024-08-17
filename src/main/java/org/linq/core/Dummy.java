package org.linq.core;

public class Dummy {

    public String name;

    public String surname;

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

    public String surname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }
}
