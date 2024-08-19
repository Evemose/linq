package org.linq.core.operand;

import java.lang.reflect.code.Value;
import java.lang.reflect.code.op.CoreOp;
import java.util.Map;
import org.linq.core.exceptions.UncapturedValueException;
import org.linq.core.util.Operands;
import org.linq.core.util.Values;

class IntegerStaticTransformNode extends StaticTransformNode {

    protected IntegerStaticTransformNode(StaticTransformerWithArgs transformer) {
        super(transformer);
    }

    protected IntegerStaticTransformNode(PlainValue value) {
        super(value);
    }

    public static IntegerStaticTransformNode newIntTransformNode(CoreOp.InvokeOp invokeOp, Map<Value, Object> capturedValues) {
        try {
            return new IntegerStaticTransformNode(new IntLiteralValue(Values.valueOf(invokeOp, capturedValues)));
        } catch (UncapturedValueException _) {
            var transformer = IntegerTransformer.of(invokeOp);
            var args = Operands.paramsOf(invokeOp, capturedValues);
            return new IntegerStaticTransformNode(new StaticTransformerWithArgs(transformer, args.toArray(new Operand[0])));
        }
    }

    private enum IntegerTransformer implements StaticTransformer {
        GET_INTEGER {
            @Override
            public String transform(Operand... args) {
                var name = args[0].getAsString();
                if (args.length == 1) {
                    return Integer.getInteger(name).toString();
                } else {
                    var defaultValue = args[1].getAsString();
                    var propValue = Integer.getInteger(name);
                    return propValue == null ? defaultValue : propValue.toString();
                }
            }
        },
        VALUE_OF {
            @Override
            public String transform(Operand... args) {
                // TODO add support for other radixes
                return "CAST(" + args[0].getAsString() + " AS INTEGER)";
            }
        },
        PARSE_INT {
            @Override
            public String transform(Operand... args) {
                // TODO add support for other radixes
                return "CAST(" + args[0].getAsString() + " AS INTEGER)";
            }
        },
        PARSE_UNSIGNED_INT {
            @Override
            public String transform(Operand... args) {
                // TODO add support for other radixes
                return "CAST(" + args[0].getAsString() + " AS UNSIGNED)";
            }
        },
        BIT_COUNT {
            @Override
            public String transform(Operand... args) {
                // TODO use BIT_COUNT for T-SQL
                var num = args[0].getAsString();
                var builder = new StringBuilder("CAST(");
                for (int i = 0; i < 32; i++) {
                    builder.append("((").append(num).append(" & ").append(Math.pow(2,i)).append(") >> ").append(i).append(") + ");
                }
                builder.delete(builder.length() - 3, builder.length());
                builder.append(" AS INTEGER)");
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
                return "SIGN((" + IntegerTransformer.toUnsigned(args[0].getAsString()) + ") - ("
                    + IntegerTransformer.toUnsigned(args[1].getAsString()) + "))";
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
                    " = 0 THEN 32 ELSE FLOOR(LOG(ABS(" + args[0].getAsString() + "), 2)) END";
            }
        },
        NUMBER_OF_TRAILING_ZEROS {
            @Override
            public String transform(Operand... args) {
                return "CASE WHEN " + args[0].getAsString() +
                    " = 0 THEN 32 ELSE FLOOR(LOG(ABS(" + args[0].getAsString() + " & -" + args[0].getAsString() + ", 2)) END";
            }
        },
        REMAINDER_UNSIGNED {
            @Override
            public String transform(Operand... args) {
                return "MOD(" + IntegerTransformer.toUnsigned(args[0].getAsString()) + ", " + args[1].getAsString() + ")";
            }
        },
        REVERSE {
            @Override
            public String transform(Operand... args) {
                return "CAST(REVERSE(" + args[0].getAsString() + ") AS INTEGER)";
            }
        },
        REVERSE_BYTES {
            @Override
            public String transform(Operand... args) {
                return "CAST(REVERSE(BIN(" + args[0].getAsString() + ")) AS INTEGER)";
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
                return "(((" + num + " << " + shift + ") & " + Integer.MIN_VALUE + ") | ((" + num + " >> (32 - " + shift + ") & " + Integer.MIN_VALUE + "))";
            }
        },
        ROTATE_RIGHT {
            @Override
            public String transform(Operand... args) {
                var num = args[0].getAsString();
                var shift = args[1].getAsString();
                return "(((" + num + " >> " + shift + ") & " + Integer.MIN_VALUE + ") | ((" + num + " << (32 - " + shift + ") & " + Integer.MIN_VALUE + "))";
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
            @Override
            public String transform(Operand... args) {
                return "CAST(" + IntegerTransformer.toUnsigned(args[0].getAsString()) + " AS VARCHAR)";
            }
        },
        TO_UNSIGNED_LONG {
            @Override
            public String transform(Operand... args) {
                return "CAST(" + IntegerTransformer.toUnsigned(args[0].getAsString()) + " AS BIGINT)";
            }
        },;

        private static String toUnsigned(String s) {
            return "CAST(" + s + " AS UNSIGNED)";
        }

        public static StaticTransformer of(CoreOp.InvokeOp invokeOp) {
            return switch (invokeOp.invokeDescriptor().name()) {
                case "getInteger" -> GET_INTEGER;
                case "valueOf" -> VALUE_OF;
                case "parseInt" -> PARSE_INT;
                case "parseUnsignedInt" -> PARSE_UNSIGNED_INT;
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
                case "toUnsignedLong" -> TO_UNSIGNED_LONG;
                default -> throw new IllegalArgumentException("Unsupported method");
            };
        }
    }

    private static class IntLiteralValue extends LiteralValue<Integer> {
        IntLiteralValue(Integer value) {
            super(value);
        }
    }
}
