package org.linq.core.operand;

import java.lang.reflect.code.Op;
import java.lang.reflect.code.Value;
import java.lang.reflect.code.op.CoreOp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.linq.core.exceptions.UncapturedValueException;
import org.linq.core.util.Values;

class StringOperand extends AbstractOperand {

    StringOperand(Op op, Map<Value, Object> capturedValues) {
        super(rootVal(op, capturedValues), transformsOf(op, capturedValues));
    }

    private static PlainValue rootVal(Op op, Map<Value, Object> capturedValues) {
        var rootOp = rootOp(op);

        return switch (rootOp) {
            case CoreOp.FieldAccessOp fieldAccessOp -> {
                try {
                    yield new StringLiteralValue(Values.valueOf(fieldAccessOp, capturedValues));
                } catch (UncapturedValueException _) {
                    yield new ColumnValue(fieldAccessOp.fieldDescriptor().name());
                }
            }
            case CoreOp.InvokeOp invokeOp -> {
                try {
                    yield new StringLiteralValue(Values.valueOf(invokeOp, capturedValues));
                } catch (UncapturedValueException _) {
                    yield new ColumnValue(accessorToFieldName(invokeOp.invokeDescriptor().name()));
                }
            }
            case CoreOp.VarAccessOp varAccessOp -> new StringLiteralValue(Values.valueOf(varAccessOp, capturedValues));
            case CoreOp.ConstantOp constantOp -> new StringLiteralValue((String) constantOp.value());
            default -> throw new IllegalArgumentException("Unsupported field type");
        };
    }

    private static List<TransformerWithArgs> transformsOf(Op op, Map<Value, Object> capturedValues) {
        var transforms = new ArrayList<TransformerWithArgs>();
        var currentOp = op;
        while (currentOp instanceof CoreOp.InvokeOp invokeOp
            && invokeOp.operands().getFirst() instanceof Op.Result result
            && !(result.op() instanceof CoreOp.VarAccessOp)
        ) {
            var transformer = StringTransformer.of(invokeOp);
            var args = invokeOp.operands().stream()
                .skip(1)
                .map(arg -> {
                    if (arg instanceof Op.Result argResult) {
                        return Values.valueOf(argResult.op(), capturedValues);
                    }
                    throw new IllegalArgumentException("Unsupported parameter type");
                })
                .toArray();
            transforms.add(new TransformerWithArgs(transformer, args));
            currentOp = result.op();
        }
        return transforms;
    }

    private enum StringTransformer implements Transformer {
        TO_UPPER_CASE {
            @Override
            public String transform(String fieldName, Object... args) {
                return "UPPER(" + fieldName + ")";
            }
        },
        TO_LOWER_CASE {
            @Override
            public String transform(String fieldName, Object... args) {
                return "LOWER(" + fieldName + ")";
            }
        },
        TRIM {
            @Override
            public String transform(String fieldName, Object... args) {
                return "RTRIM(LTRIM(" + fieldName + "))";
            }
        },
        LENGTH {
            @Override
            public String transform(String fieldName, Object... args) {
                return "LENGTH(" + fieldName + ")";
            }
        },
        INDENT {
            @Override
            public String transform(String fieldName, Object... args) {
                var indent = (int) args[0];
                if (indent < 0) {
                    return "SUBSTR(LTRIM(SUBSTR(%s, 1, n)), 1, LENGTH(%s) - n + LENGTH(LTRIM(SUBSTR(%s, 1, n)))) || SUBSTR(%s, n + 1)"
                        .formatted(fieldName, fieldName, fieldName, fieldName);
                }
                return "RPAD(" + fieldName + ", " + (indent + fieldName.length()) + ", ' ')";
            }
        },
        INTERN {
            @Override
            public String transform(String fieldName, Object... args) {
                return fieldName;
            }
        },
        REPEAT {
            @Override
            public String transform(String fieldName, Object... args) {
                var count = (int) args[0];
                return "RPAD('', " + count + ", " + fieldName + ")";
            }
        },
        REPLACE {
            @Override
            public String transform(String fieldName, Object... args) {
                var oldString = (String) args[0];
                var newString = (String) args[1];
                return "REPLACE(" + fieldName + ", '" + oldString + "', '" + newString + "')";
            }
        },
        REPLACE_ALL {
            @Override
            public String transform(String fieldName, Object... args) {
                var regex = (String) args[0];
                var replacement = (String) args[1];
                return "REGEXP_REPLACE(" + fieldName + ", '" + regex + "', '" + replacement + "')";
            }
        },
        REPLACE_FIRST {
            @Override
            public String transform(String fieldName, Object... args) {
                var regex = (String) args[0];
                var replacement = (String) args[1];
                return "REGEXP_REPLACE(" + fieldName + ", '" + regex + "', '" + replacement + "', 1)";
            }
        },
        SUBSTRING {
            @Override
            public String transform(String fieldName, Object... args) {
                var start = ((int) args[0]);
                var end = args.length > 1 ? (int) args[1] : Integer.MAX_VALUE;
                var length = end - start;
                return "SUBSTR(" + fieldName + ", " + (start + 1) + ", " + length + ")";
            }
        },
        CONCAT {
            @Override
            public String transform(String fieldName, Object... args) {
                return "CONCAT(" + fieldName + ", " + args[0] + ")";
            }
        },
        RESOLVE_CONSTANT_DESC {
            @Override
            public String transform(String fieldName, Object... args) {
                return fieldName;
            }
        },
        STRIP {
            @Override
            public String transform(String fieldName, Object... args) {
                return "LTRIM(RTRIM(" + fieldName + "))";
            }
        },
        STRIP_LEADING {
            @Override
            public String transform(String fieldName, Object... args) {
                return "LTRIM(" + fieldName + ")";
            }
        },
        STRIP_INDENT {
            @Override
            public String transform(String fieldName, Object... args) {
                return "SUBSTR(" + fieldName + ", n)";
            }
        },
        CHAR_AT {
            @Override
            public String transform(String fieldName, Object... args) {
                var index = (int) args[0];
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
