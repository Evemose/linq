package org.linq.core.query;

import java.lang.reflect.code.Block;
import java.lang.reflect.code.Op;
import java.lang.reflect.code.TypeElement;
import java.lang.reflect.code.op.CoreOp;
import java.lang.reflect.code.type.ClassType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.linq.core.util.Values;

abstract class Operand {

    private final List<TransformerWithArgs> transforms;

    private final PlainValue plainVal;

    protected Operand(PlainValue plainVal, List<TransformerWithArgs> transforms) {
        this.plainVal = plainVal;
        this.transforms = transforms;
    }

    public String getFieldAsString() {
        return transforms.stream().reduce(plainVal.getValueAsString(), (s, t) -> t.transform(s), (s1, _) -> s1);
    }

    public static Operand of(Op op, Map<java.lang.reflect.code.Value, Object> capturedValues) {
        return switch (rootColumnType(op)) {
            case ClassType classType when classType.toClassName().equals(String.class.getName()) -> new StringOperand(op, capturedValues);
            default -> throw new IllegalArgumentException("Unsupported field type");
        };
    }

    protected static Op rootOp(Op op) {
        var currentOp = op;
        if (currentOp instanceof CoreOp.ConstantOp) {
            return currentOp;
        }
        while (currentOp.operands().getFirst() instanceof Op.Result result && (result.op() instanceof CoreOp.FieldAccessOp || result.op() instanceof CoreOp.InvokeOp)) {
            currentOp = result.op();
        }
        return currentOp;
    }

    private static TypeElement rootColumnType(Op op) {
        var rootOp = rootOp(op);
        if (rootOp instanceof CoreOp.FieldAccessOp fieldAccessOp) {
            return fieldAccessOp.fieldDescriptor().type();
        } else if (rootOp instanceof CoreOp.InvokeOp invokeOp) {
            return invokeOp.invokeDescriptor().type().returnType();
        } else {
            return rootOp.resultType();
        }
    }

    protected interface Transformer {
        String transform(String fieldName, Object... args);
    }

    protected static class TransformerWithArgs {

        private final Transformer transformer;

        private final Object[] args;

        private TransformerWithArgs(Transformer transformer, Object... args) {
            this.transformer = transformer;
            this.args = args;
        }

        public String transform(String fieldName) {
            return transformer.transform(fieldName, args);
        }
    }

    protected static String accessorToFieldName(String getter) {
        if (!getter.startsWith("get")) {
            return getter;
        }
        return getter.substring(3, 4).toLowerCase() + getter.substring(4);
    }

    private static class StringOperand extends Operand {

        private StringOperand(Op op, Map<java.lang.reflect.code.Value, Object> capturedValues) {
            super(rootVal(op, capturedValues), transformsOf(op, capturedValues));
        }

        private static PlainValue rootVal(Op op, Map<java.lang.reflect.code.Value, Object> capturedValues) {
            if (op instanceof CoreOp.ConstantOp constantOp) {
                return new StringConstantValue((String) constantOp.value());
            }

            var rootOp = rootOp(op);

            return switch (rootOp) {
                case CoreOp.FieldAccessOp fieldAccessOp -> new ColumnValue(fieldAccessOp.fieldDescriptor().name());
                case CoreOp.InvokeOp invokeOp -> new ColumnValue(accessorToFieldName(invokeOp.invokeDescriptor().name()));
                case CoreOp.VarAccessOp varAccessOp -> StringCapturedValue.newValue(varAccessOp, capturedValues);
                default -> throw new IllegalArgumentException("Unsupported field type");
            };
        }

        private static List<TransformerWithArgs> transformsOf(Op op, Map<java.lang.reflect.code.Value, Object> capturedValues) {
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
                            var argOp = argResult.op();
                            if (argOp instanceof CoreOp.ConstantOp constantOp) {
                                return constantOp.value();
                            } else if (argOp instanceof CoreOp.VarAccessOp varAccessOp) {
                                return Values.valueOf(varAccessOp, capturedValues);
                            }
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
                    var start = (int) args[0];
                    var end = args.length > 1 ? (int) args[1] : Integer.MAX_VALUE;
                    return "SUBSTR(" + fieldName + ", " + start + ", " + end + ")";
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

        private static final class StringConstantValue extends ConstantValue {

            private StringConstantValue(String value) {
                super(value);
            }

            @Override
            public String getValueAsString() {
                return "'" + value + "'";
            }
        }

        public final static class StringCapturedValue extends CapturedValue {
            private StringCapturedValue(String value) {
                super(value);
            }

            public static StringCapturedValue newValue(CoreOp.VarAccessOp varAccessOp, Map<java.lang.reflect.code.Value, Object> capturedValues) {
                return new StringCapturedValue(CapturedValue.of(varAccessOp, capturedValues).value);
            }

            @Override
            public String getValueAsString() {
                return "'" + value + "'";
            }
        }
    }

    protected sealed interface PlainValue permits ConstantValue, ColumnValue, CapturedValue {
        String getValueAsString();
    }

    private static sealed class ConstantValue implements PlainValue permits StringOperand.StringConstantValue {

        protected final String value;

        private ConstantValue(String value) {
            this.value = value;
        }

        @Override
        public String getValueAsString() {
            return value;
        }
    }

    private record ColumnValue(String fieldName) implements PlainValue {

        @Override
        public String getValueAsString() {
            return "$" + fieldName;
        }
    }

    private static sealed class CapturedValue implements PlainValue permits StringOperand.StringCapturedValue {

        protected final String value;

        private CapturedValue(String value) {
            this.value = value;
        }

        public static CapturedValue of(Op op, Map<java.lang.reflect.code.Value, Object> capturedValues) {
            var parentLambda = op;

            while (parentLambda != null && !(parentLambda instanceof CoreOp.LambdaOp)) {
                parentLambda = parentLambda.parent().parentBody().parentOp();
            }

            if (parentLambda instanceof CoreOp.LambdaOp lambdaOp) {
                if (op instanceof CoreOp.FieldAccessOp fieldAccessOp) {
                    return new CapturedValue(lambdaOp.capturedValues().stream()
                        .filter(Op.Result.class::isInstance)
                        .filter(result -> ((Op.Result) result).op() instanceof CoreOp.VarAccessOp varAccessOp &&
                            varAccessOp.varOp().varName().equals(fieldAccessOp.fieldDescriptor().name()))
                        .map(result -> ((Op.Result) result).op().toString())
                        .findFirst()
                        .orElseThrow());
                } else if (op instanceof CoreOp.VarAccessOp varAccessOp) {
                    var result = varAccessOp.varOp().result();
                    var capturedValue = capturedValues.get(result);
                    if (capturedValue instanceof CoreOp.Var<?> var) {
                        if (var.value() instanceof String) {
                            return new CapturedValue((String) var.value());
                        }
                    }
                    throw new IllegalArgumentException("Cannot resolve captured value");
                } else {
                    throw new IllegalArgumentException("Unsupported captured value");
                }
            } else {
                throw new IllegalArgumentException("Unsupported captured value");
            }
        }

        @Override
        public String getValueAsString() {
            return value;
        }
    }
}
