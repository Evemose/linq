package org.linq.core;

import org.linq.core.query.DbStream;

public class Main {

    public static void main(String[] args) {
        System.out.println(
            new DbStream<>(Dummy.class)
                .filter(t -> t.name().substring(
                    new Dummy2().getStartIdx(),
                    Dummy2.endIdx()
                ).concat(Integer.valueOf(4).toString()).toUpperCase().matches("[A-E]{1,10}\\d*"))
                .filter(t -> t.name().strip().endsWith(t.name().substring(1)) || t.surname().stripLeading().startsWith("d"))
                .filter(t -> !t.name().isBlank())
                //.filter(t -> Integer.bitCount(t.name().length()) == 2)
                //.filter(t -> !t.name().matches("\\d+") || Integer.parseUnsignedInt(t.name()) == -2147483648)
                .filter(t -> !Long.toUnsignedString(t.num).contains("-"))
                .filter(t -> 2 != 1)
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
