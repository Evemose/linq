package org.linq.core.operand;

enum NumericStaticTransformer implements StaticTransformer {
    VALUE_OF {
        @Override
        public String transform(Operand... args) {
            // TODO add support for other radixes
            return "CAST(" + args[0].getAsString() + " AS %s)".formatted(getNumericType(args));
        }
    },
    PARSE {
        @Override
        public String transform(Operand... args) {
            // TODO add support for other radixes
            return "CAST(" + args[0].getAsString() + " AS %s)".formatted(getNumericType(args));
        }
    },
    PARSE_UNSIGNED {
        @Override
        public String transform(Operand... args) {
            var highestBit = getHighestBit(getNumericType(args));
            // TODO add support for other radixes
            return NumericStaticTransformer.toUnsigned("CAST( " + args[0].getAsString() + " AS BIGINT)", highestBit);
        }
    },
    BIT_COUNT {
        @Override
        public String transform(Operand... args) {
            // TODO use BIT_COUNT for T-SQL
            var num = args[0].getAsString();
            var builder = new StringBuilder("CAST(");
            for (int i = 0; i < 64; i++) {
                builder.append("((").append(num).append(" & ").append((int) Math.pow(2, i)).append(") >> ").append(i).append(") + ");
            }
            builder.delete(builder.length() - 3, builder.length());
            builder.append(" AS %s)".formatted(getNumericType(args)));
            return builder.toString();
        }
    },
    COMPARE {
        @Override
        public String transform(Operand... args) {
            return "SIGN(" + args[0].getAsString() + " - " + args[1].getAsString() + ")";
        }
    },
    COMPARE_UNSIGNED {
        @Override
        public String transform(Operand... args) {
            var highestBit = getHighestBit(getNumericType(args));
            return "SIGN((" + NumericStaticTransformer.toUnsigned(args[0].getAsString(), highestBit) + ") - ("
                + NumericStaticTransformer.toUnsigned(args[1].getAsString(), highestBit) + "))";
        }
    },
    HIGHEST_ONE_BIT {
        @Override
        public String transform(Operand... args) {
            return "POWER(2, FLOOR(LOG(ABS(" + args[0].getAsString() + "), 2)))";
        }
    },
    LOWEST_ONE_BIT {
        @Override
        public String transform(Operand... args) {
            return "POWER(2, FLOOR(LOG(ABS(" + args[0].getAsString() + " & -" + args[0].getAsString() + ", 2)))";
        }
    },
    MAX {
        @Override
        public String transform(Operand... args) {
            return "MAX(" + args[0].getAsString() + ", " + args[1].getAsString() + ")";
        }
    },
    MIN {
        @Override
        public String transform(Operand... args) {
            return "MIN(" + args[0].getAsString() + ", " + args[1].getAsString() + ")";
        }
    },
    NUMBER_OF_LEADING_ZEROS {
        @Override
        public String transform(Operand... args) {
            return "CASE WHEN " + args[0].getAsString() +
                " = 0 THEN 64 ELSE FLOOR(LOG(ABS(" + args[0].getAsString() + "), 2)) END";
        }
    },
    NUMBER_OF_TRAILING_ZEROS {
        @Override
        public String transform(Operand... args) {
            return "CASE WHEN " + args[0].getAsString() +
                " = 0 THEN 64 ELSE FLOOR(LOG(ABS(" + args[0].getAsString() + " & -" + args[0].getAsString() + ", 2)) END";
        }
    },
    REMAINDER_UNSIGNED {
        @Override
        public String transform(Operand... args) {
            var highestBit = getHighestBit(getNumericType(args));
            return "MOD(" + NumericStaticTransformer.toUnsigned(args[0].getAsString(), highestBit) + ", " + args[1].getAsString() + ")";
        }
    },
    REVERSE {
        @Override
        public String transform(Operand... args) {
            return "CAST(REVERSE(" + args[0].getAsString() + ") AS %s)".formatted(getNumericType(args));
        }
    },
    REVERSE_BYTES {
        @Override
        public String transform(Operand... args) {
            return "CAST(REVERSE(BIN(" + args[0].getAsString() + ")) AS %s)".formatted(getNumericType(args));
        }
    },
    SIGNUM {
        @Override
        public String transform(Operand... args) {
            return "SIGN(" + args[0].getAsString() + ")";
        }
    },
    SUM {
        @Override
        public String transform(Operand... args) {
            return args[0].getAsString() + " + " + args[1].getAsString();
        }
    },
    ROTATE_LEFT {
        @Override
        public String transform(Operand... args) {
            var num = args[0].getAsString();
            var shift = args[1].getAsString();
            return "(((" + num + " << " + shift + ") & " + Integer.MIN_VALUE + ") | ((" + num + " >> (32 - "
                + shift + ") & " + Integer.MIN_VALUE + "))";
        }
    },
    ROTATE_RIGHT {
        @Override
        public String transform(Operand... args) {
            var num = args[0].getAsString();
            var shift = args[1].getAsString();
            return "(((" + num + " >> " + shift + ") & " + Integer.MIN_VALUE + ") | ((" + num + " << (32 - "
                + shift + ") & " + Integer.MIN_VALUE + "))";
        }
    },
    TO_BINARY_STRING {
        @Override
        public String transform(Operand... args) {
            return "BIN(" + args[0].getAsString() + ")";
        }
    },
    TO_HEX_STRING {
        @Override
        public String transform(Operand... args) {
            return "HEX(" + args[0].getAsString() + ")";
        }
    },
    TO_OCTAL_STRING {
        @Override
        public String transform(Operand... args) {
            return "OCT(" + args[0].getAsString() + ")";
        }
    },
    TO_STRING {
        @Override
        public String transform(Operand... args) {
            return "CAST(" + args[0].getAsString() + " AS VARCHAR)";
        }
    },
    TO_UNSIGNED_STRING {
        // FIXME
        @Override
        public String transform(Operand... args) {
            var highestBit = getHighestBit(getNumericType(args));
            return "CAST(" + NumericStaticTransformer.toUnsigned(args[0].getAsString(), highestBit) + " AS VARCHAR)";
        }
    };

    private static String toUnsigned(String s, byte highestBit) {
        var maxValue = (long) Math.pow(2, highestBit) - 1;
        // not sure if this is the correct way to parse unsigned int, but bitwise it's the same
        return "CASE WHEN %s < 0 THEN (ABS(CAST(%s AS NUMERIC) + %d)) ELSE %s END".formatted(s, s, maxValue, s);
    }

    private static String getNumericType(Operand... args) {
        return args[args.length - 1].getAsString().toUpperCase();
    }

    private static byte getHighestBit(String numericType) {
        return switch (numericType) {
            case "TINYINT" -> 8;
            case "SMALLINT" -> 16;
            case "INTEGER" -> 32;
            case "BIGINT" -> 64;
            default -> throw new IllegalArgumentException("Unknown numeric type: " + numericType);
        };
    }

    public static NumericStaticTransformer of(String methodName) {
        return switch (methodName) {
            case "valueOf" -> VALUE_OF;
            case "parseLong",  "parseInt", "parseShort", "parseByte" -> PARSE;
            case "parseUnsignedInt", "parseUnsignedLong" -> PARSE_UNSIGNED;
            case "bitCount" -> BIT_COUNT;
            case "compare" -> COMPARE;
            case "compareUnsigned" -> COMPARE_UNSIGNED;
            case "highestOneBit" -> HIGHEST_ONE_BIT;
            case "lowestOneBit" -> LOWEST_ONE_BIT;
            case "max" -> MAX;
            case "min" -> MIN;
            case "numberOfLeadingZeros" -> NUMBER_OF_LEADING_ZEROS;
            case "numberOfTrailingZeros" -> NUMBER_OF_TRAILING_ZEROS;
            case "remainderUnsigned" -> REMAINDER_UNSIGNED;
            case "reverse" -> REVERSE;
            case "reverseBytes" -> REVERSE_BYTES;
            case "signum" -> SIGNUM;
            case "sum" -> SUM;
            case "rotateLeft" -> ROTATE_LEFT;
            case "rotateRight" -> ROTATE_RIGHT;
            case "toBinaryString" -> TO_BINARY_STRING;
            case "toHexString" -> TO_HEX_STRING;
            case "toOctalString" -> TO_OCTAL_STRING;
            case "toString" -> TO_STRING;
            case "toUnsignedString" -> TO_UNSIGNED_STRING;
            default -> throw new IllegalArgumentException("Unknown method: " + methodName);
        };
    }
}
