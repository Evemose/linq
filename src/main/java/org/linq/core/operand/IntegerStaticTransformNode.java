package org.linq.core.operand;

import java.lang.reflect.code.Value;
import java.lang.reflect.code.op.CoreOp;
import java.util.ArrayList;
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
            var args = new ArrayList<>(Operands.paramsOf(invokeOp, capturedValues));
            args.add(new TransformNode(StringLiteralValue.ofRaw("INTEGER")) {});
            return new IntegerStaticTransformNode(new StaticTransformerWithArgs(transformer, args.toArray(new Operand[0])));
        }
    }

    private enum IntegerTransformer implements StaticTransformer {
        GET_INTEGER {
            @Override
            public String transform(Operand... args) {
                var name = args[0].getAsString();
                if (args.length == 2) {
                    return Integer.getInteger(name).toString();
                } else {
                    var defaultValue = args[1].getAsString();
                    var propValue = Integer.getInteger(name);
                    return propValue == null ? defaultValue : propValue.toString();
                }
            }
        },
        TO_UNSIGNED_LONG {
            @Override
            public String transform(Operand... args) {
                return "CAST(CAST( " + args[0].getAsString() + " AS NUMERIC) AS BIGINT)";
            }
        },;

        public static StaticTransformer of(CoreOp.InvokeOp invokeOp) {
            return switch (invokeOp.invokeDescriptor().name()) {
                case "getInteger" -> GET_INTEGER;
                case "toUnsignedLong" -> TO_UNSIGNED_LONG;
                default -> NumericStaticTransformer.of(invokeOp.invokeDescriptor().name());
            };
        }
    }

    private static class IntLiteralValue extends LiteralValue<Integer> {
        IntLiteralValue(Integer value) {
            super(value);
        }
    }
}
