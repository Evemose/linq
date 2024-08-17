package org.linq.core;

import org.linq.core.query.DbStream;

public class Main {

    public static void main(String[] args) {
        System.out.println(
            new DbStream<>(Dummy.class)
                .filter(t -> t.name().substring(new Dummy2().getStartIdx(), Dummy2.endIdx()).concat(t.surname()).toUpperCase().matches("[A-E]{1,5}\\d*"))
                .filter(t -> t.name().equals(t.surname()))
                .filter(t -> !t.name().isBlank())
                .toList()
        );
    }

    public static class Dummy2 {
        private static final int endIdx = 2;
        public final int startIdx = 1;

        public static int endIdx() {
            return endIdx;
        }

        public int getStartIdx() {
            return startIdx;
        }
    }
}
