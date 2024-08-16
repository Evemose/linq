package org.linq.core;

import org.linq.core.query.DbStream;

public class Main {

    public static void main(String[] args) {
        var startIdx = 1;
        System.out.println(
            new DbStream<>(Dummy.class)
                .filter(t -> t.name().substring(startIdx).toUpperCase().matches("([A-E]{1,3})|(\\s*)"))
                .filter(t -> !t.name().isBlank())
                .toList()
        );
    }
}
