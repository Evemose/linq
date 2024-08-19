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

public class NumberTransformNode extends TransformNode {

    private NumberTransformNode(PlainValue plainVal) {
        super(plainVal);
    }

    private NumberTransformNode(Operand prevOperand, TransformerWithArgs transform) {
        super(prevOperand, transform);
    }

    static NumberTransformNode newIntTransformNode(Op op, Map<Value, Object> capturedValues) {
        if (Ops.isTerminal(op)) {
            return new NumberTransformNode(new IntLiteralValue(Values.valueOf(op, capturedValues)));
        } else if (Ops.isColumnAccessor(op, capturedValues)) {
            return new NumberTransformNode(new ColumnValue(Extracts.accessorToFieldName(op)));
        }

        try {
            return new NumberTransformNode(new IntLiteralValue(Values.valueOf(op, capturedValues)));
        } catch (UncapturedValueException _) {
            return new NumberTransformNode(
                Operand.of(Ops.prevOp(op).orElseThrow(), capturedValues),
                transformerWithArgs((CoreOp.InvokeOp) op, capturedValues)
            );
        }
    }

    private static TransformerWithArgs transformerWithArgs(CoreOp.InvokeOp invokeOp, Map<Value, Object> capturedValues) {
        var transformer = IntTransformer.of(invokeOp);
        var args = Operands.paramsOf(invokeOp, capturedValues);
        return new TransformerWithArgs(transformer, args.toArray(new Operand[0]));
    }

    private enum IntTransformer implements Transformer {
        ABS {
            @Override
            public String transform(String fieldName, Operand... args) {
                return "ABS(" + fieldName + ")";
            }
        },
        CEIL {
            @Override
            public String transform(String fieldName, Operand... args) {
                return "CEIL(" + fieldName + ")";
            }
        },
        FLOOR {
            @Override
            public String transform(String fieldName, Operand... args) {
                return "FLOOR(" + fieldName + ")";
            }
        },
        ROUND {
            @Override
            public String transform(String fieldName, Operand... args) {
                return "ROUND(" + fieldName + ")";
            }
        },
        SIGNUM {
            @Override
            public String transform(String fieldName, Operand... args) {
                return "SIGNUM(" + fieldName + ")";
            }
        },
        SQRT {
            @Override
            public String transform(String fieldName, Operand... args) {
                return "SQRT(" + fieldName + ")";
            }
        },
        MAX {
            @Override
            public String transform(String fieldName, Operand... args) {
                return "MAX(" + fieldName + ")";
            }
        },
        MIN {
            @Override
            public String transform(String fieldName, Operand... args) {
                return "MIN(" + fieldName + ")";
            }
        };

        public static IntTransformer of(CoreOp.InvokeOp invokeOp) {
            return switch (invokeOp.invokeDescriptor().name()) {
                case "abs" -> ABS;
                case "ceil" -> CEIL;
                case "floor" -> FLOOR;
                case "round" -> ROUND;
                case "signum" -> SIGNUM;
                case "sqrt" -> SQRT;
                case "max" -> MAX;
                case "min" -> MIN;
                default -> throw new IllegalArgumentException("Unsupported field type");
            };
        }
    }

    private static class IntLiteralValue extends LiteralValue<Integer> {

        private IntLiteralValue(Integer value) {
            super(value);
        }

    }
}
