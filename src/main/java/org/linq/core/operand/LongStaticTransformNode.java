package org.linq.core.operand;

import java.lang.reflect.code.Value;
import java.lang.reflect.code.op.CoreOp;
import java.util.ArrayList;
import java.util.Map;
import org.linq.core.exceptions.UncapturedValueException;
import org.linq.core.util.Operands;
import org.linq.core.util.Values;

class LongStaticTransformNode extends StaticTransformNode {

    protected LongStaticTransformNode(PlainValue value) {
        super(value);
    }

    protected LongStaticTransformNode(StaticTransformerWithArgs transformer) {
        super(transformer);
    }

    public static LongStaticTransformNode newLongTransformNode(CoreOp.InvokeOp invokeOp, Map<Value, Object> capturedValues) {
        try {
            return new LongStaticTransformNode(new LongLiteralValue(Values.valueOf(invokeOp, capturedValues)));
        } catch (UncapturedValueException _) {
            var transformer = LongTransformer.of(invokeOp);
            var args = new ArrayList<>(Operands.paramsOf(invokeOp, capturedValues));
            args.add(new TransformNode(StringLiteralValue.ofRaw("BIGINT")) {});
            return new LongStaticTransformNode(new StaticTransformerWithArgs(transformer, args.toArray(new Operand[0])));
        }
    }

    private enum LongTransformer implements StaticTransformer {
        GET_LONG {
            @Override
            public String transform(Operand... args) {
                var name = args[0].getAsString();
                if (args.length == 2) {
                    return Long.getLong(name).toString();
                } else {
                    var defaultValue = args[1].getAsString();
                    var propValue = Long.getLong(name);
                    return propValue == null ? defaultValue : propValue.toString();
                }
            }
        };

        public static StaticTransformer of(CoreOp.InvokeOp invokeOp) {
            return switch (invokeOp.invokeDescriptor().name()) {
                case "getLong" -> GET_LONG;
                default -> NumericStaticTransformer.of(invokeOp.invokeDescriptor().name());
            };
        }
    }

    private static class LongLiteralValue extends LiteralValue<Long> {
        LongLiteralValue(Long value) {
            super(value);
        }
    }
}
