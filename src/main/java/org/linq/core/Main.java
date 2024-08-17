package org.linq.core;

import org.linq.core.query.DbStream;

public class Main {

    public static void main(String[] args) {
        var startIdx = 1;
        var dummy = new Dummy2();
        System.out.println(
            new DbStream<>(Dummy.class)
                .filter(t -> t.name().substring(new Dummy2().getStartIdx(), Dummy2.endIdx()).toUpperCase().matches("([A-E]{1,3})"))
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
