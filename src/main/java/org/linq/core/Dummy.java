package org.linq.core;

public class Dummy {

    public String name;

    public String surname;

    public long num;

    @Override
    public String toString() {
        return "Dummy{" +
            "name='" + name + '\'' +
            ", surname='" + surname + '\'' +
            ", num=" + num +
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
