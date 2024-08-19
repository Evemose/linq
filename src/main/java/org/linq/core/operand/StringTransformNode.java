package org.linq.core.operand;

import java.lang.reflect.code.Op;
import java.lang.reflect.code.Value;
import java.lang.reflect.code.op.CoreOp;
import java.util.Map;
import org.linq.core.exceptions.UncapturedValueException;
import org.linq.core.util.Extracts;
import org.linq.core.util.Operands;
import org.linq.core.util.Ops;
import org.linq.core.util.Values;

class StringTransformNode extends TransformNode {

    private StringTransformNode(PlainValue plainVal) {
        super(plainVal);
    }

    private StringTransformNode(Operand prevOperand, TransformerWithArgs transform) {
        super(prevOperand, transform);
    }

    static StringTransformNode newStringTransformNode(Op op, Map<Value, Object> capturedValues) {
        if (Ops.isTerminal(op)) {
            return new StringTransformNode(new StringLiteralValue(Values.valueOf(op, capturedValues)));
        } else if (Ops.isColumnAccessor(op, capturedValues)) {
            return new StringTransformNode(new ColumnValue(Extracts.accessorToFieldName(op)));
        }

        try {
            return new StringTransformNode(new StringLiteralValue(Values.valueOf(op, capturedValues)));
        } catch (UncapturedValueException _) {
            return new StringTransformNode(
                Operand.of(Ops.prevOp(op).orElseThrow(), capturedValues),
                transformerWithArgs((CoreOp.InvokeOp) op, capturedValues)
            );
        }
    }

    private static TransformerWithArgs transformerWithArgs(CoreOp.InvokeOp invokeOp, Map<Value, Object> capturedValues) {
        var transformer = StringTransformer.of(invokeOp);
        var args = Operands.paramsOf(invokeOp, capturedValues);
        return new TransformerWithArgs(transformer, args.toArray(new Operand[0]));
    }

    private enum StringTransformer implements Transformer {
        TO_UPPER_CASE {
            @Override
            public String transform(String fieldName, Operand... args) {
                return "UPPER(" + fieldName + ")";
            }
        },
        TO_LOWER_CASE {
            @Override
            public String transform(String fieldName, Operand... args) {
                return "LOWER(" + fieldName + ")";
            }
        },
        TRIM {
            @Override
            public String transform(String fieldName, Operand... args) {
                return "RTRIM(LTRIM(" + fieldName + "))";
            }
        },
        LENGTH {
            @Override
            public String transform(String fieldName, Operand... args) {
                return "LENGTH(" + fieldName + ")";
            }
        },
        INDENT {
            @Override
            public String transform(String fieldName, Operand... args) {
                var indent = args[0].getAsString();
                // TODO handle negative indent
                return "RPAD(" + fieldName + ", (" + (indent + " + " + fieldName.length()) + "), ' ')";
            }
        },
        INTERN {
            @Override
            public String transform(String fieldName, Operand... args) {
                return fieldName;
            }
        },
        REPEAT {
            @Override
            public String transform(String fieldName, Operand... args) {
                var count = args[0].getAsString();
                return "RPAD('', " + count + ", " + fieldName + ")";
            }
        },
        REPLACE {
            @Override
            public String transform(String fieldName, Operand... args) {
                var oldString = args[0].getAsString();
                var newString = args[1].getAsString();
                return "REPLACE(" + fieldName + ", '" + oldString + "', '" + newString + "')";
            }
        },
        REPLACE_ALL {
            @Override
            public String transform(String fieldName, Operand... args) {
                var regex = args[0].getAsString();
                var replacement = args[1].getAsString();
                return "REGEXP_REPLACE(" + fieldName + ", '" + regex + "', '" + replacement + "')";
            }
        },
        REPLACE_FIRST {
            @Override
            public String transform(String fieldName, Operand... args) {
                var regex = args[0].getAsString();
                var replacement = args[1].getAsString();
                return "REGEXP_REPLACE(" + fieldName + ", '" + regex + "', '" + replacement + "', 1)";
            }
        },
        SUBSTRING {
            @Override
            public String transform(String fieldName, Operand... args) {
                var start = args[0].getAsString();
                var end = args.length > 1 ? args[1].getAsString() : String.valueOf(Integer.MAX_VALUE);
                return "SUBSTR(" + fieldName + ", " + (start) + " + 1, (" +  (end + " - " + start) + "))";
            }
        },
        CONCAT {
            @Override
            public String transform(String fieldName, Operand... args) {
                return "CONCAT(" + fieldName + ", " + args[0].getAsString() + ")";
            }
        },
        RESOLVE_CONSTANT_DESC {
            @Override
            public String transform(String fieldName, Operand... args) {
                return fieldName;
            }
        },
        STRIP {
            @Override
            public String transform(String fieldName, Operand... args) {
                return "LTRIM(RTRIM(" + fieldName + "))";
            }
        },
        STRIP_LEADING {
            @Override
            public String transform(String fieldName, Operand... args) {
                return "LTRIM(" + fieldName + ")";
            }
        },
        STRIP_INDENT {
            @Override
            public String transform(String fieldName, Operand... args) {
                return "SUBSTR(" + fieldName + ", n)";
            }
        },
        CHAR_AT {
            @Override
            public String transform(String fieldName, Operand... args) {
                var index = args[0].getAsString();
                return "SUBSTR(" + fieldName + ", " + index + ", 1)";
            }
        };

        private static StringTransformer of(CoreOp.InvokeOp invokeOp) {
            return switch (invokeOp.invokeDescriptor().name()) {
                case "toUpperCase" -> TO_UPPER_CASE;
                case "toLowerCase" -> TO_LOWER_CASE;
                case "trim" -> TRIM;
                case "length" -> LENGTH;
                case "indent" -> INDENT;
                case "intern" -> INTERN;
                case "repeat" -> REPEAT;
                case "replace" -> REPLACE;
                case "replaceAll" -> REPLACE_ALL;
                case "replaceFirst" -> REPLACE_FIRST;
                case "substring" -> SUBSTRING;
                case "concat" -> CONCAT;
                case "resolveConstantDesc" -> RESOLVE_CONSTANT_DESC;
                case "strip" -> STRIP;
                case "stripLeading" -> STRIP_LEADING;
                case "stripIndent" -> STRIP_INDENT;
                case "charAt" -> CHAR_AT;
                default -> throw new IllegalArgumentException("Unsupported string transformer");
            };
        }
    }

    private static final class StringLiteralValue extends LiteralValue<String> {

        private StringLiteralValue(String value) {
            super(value);
        }

        @Override
        public String getValueAsString() {
            return "'" + value + "'";
        }
    }
}
