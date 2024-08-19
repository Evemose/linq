package org.linq.core;

import org.linq.core.query.DbStream;

public class Main {

    public static void main(String[] args) {
        System.out.println(
            new DbStream<>(Dummy.class)
                .filter(t -> t.name().substring(
                    new Dummy2().getStartIdx(),
                    Dummy2.endIdx()
                ).concat(Integer.valueOf(4).toString()).toUpperCase().matches("[A-E]{1,5}\\d*"))
                .filter(t -> t.name().strip().endsWith("e") || t.surname().stripLeading().startsWith("d"))
                .filter(t -> !t.name().isBlank())
                .filter(t -> Integer.valueOf(Integer.max(4, 5)).equals(5))
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
