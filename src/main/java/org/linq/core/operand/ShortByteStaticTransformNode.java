package org.linq.core.operand;

import java.lang.reflect.code.Value;
import java.lang.reflect.code.op.CoreOp;
import java.lang.reflect.code.type.ClassType;
import java.util.ArrayList;
import java.util.Map;
import org.linq.core.exceptions.UncapturedValueException;
import org.linq.core.util.Operands;
import org.linq.core.util.Values;

class ShortByteStaticTransformNode extends StaticTransformNode {

    private final boolean isShort;

    protected ShortByteStaticTransformNode(PlainValue value, boolean isShort) {
        super(value);
        this.isShort = isShort;
    }

    protected ShortByteStaticTransformNode(StaticTransformerWithArgs transformer, boolean isShort) {
        super(transformer);
        this.isShort = isShort;
    }

    public static ShortByteStaticTransformNode newShortByteTransformNode(CoreOp.InvokeOp invokeOp, Map<Value, Object> capturedValues) {
        var isShort = ((ClassType) invokeOp.invokeDescriptor().refType()).toClassName().equals(Short.class.getName());
        try {
            return new ShortByteStaticTransformNode(
                new ShortByteLiteralValue(Values.valueOf(invokeOp, capturedValues)),
                isShort
            );
        } catch (UncapturedValueException _) {
            var transformer = ShortByteTransformer.of(invokeOp);
            var args = new ArrayList<>(Operands.paramsOf(invokeOp, capturedValues));
            args.add(new TransformNode(StringLiteralValue.ofRaw(isShort ? "SMALLINT" : "TINYINT")) {});
            return new ShortByteStaticTransformNode(
                new StaticTransformerWithArgs(transformer, args.toArray(new Operand[0])),
                isShort
            );
        }
    }

    private final static class ShortByteLiteralValue extends LiteralValue<Short> {
        ShortByteLiteralValue(Short value) {
            super(value);
        }
    }

    private enum ShortByteTransformer implements StaticTransformer {
        TO_UNSIGNED_INT {
            @Override
            public String transform(Operand... args) {
                var value = args[0].getAsString();
                var type = args[1].getAsString();
                var maxVal = type.equals("SMALLINT") ? Short.MAX_VALUE : Byte.MAX_VALUE;
                return "CAST(CASE WHEN %s < 0 THEN %s + %d + 1 ELSE %s END AS INTEGER)".formatted(value, value, maxVal, value);
            }
        };

        public static StaticTransformer of(CoreOp.InvokeOp invokeOp) {
            return switch (invokeOp.invokeDescriptor().name()) {
                case "toUnsignedInt" -> TO_UNSIGNED_INT;
                default -> NumericStaticTransformer.of(invokeOp.invokeDescriptor().name());
            };
        }
    }
}
