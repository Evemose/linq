package org.linq.core.operand;

import java.lang.reflect.code.Value;
import java.lang.reflect.code.op.CoreOp;
import java.lang.reflect.code.type.ClassType;
import java.util.Map;
import java.util.Objects;

abstract class StaticTransformNode implements Operand {

    private final StaticTransformerWithArgs transformer;

    private final PlainValue value;

    protected StaticTransformNode(StaticTransformerWithArgs transformer) {
        this.transformer = transformer;
        this.value = null;
    }

    protected StaticTransformNode(PlainValue value) {
        this.transformer = null;
        this.value = value;
    }

    public static StaticTransformNode of(CoreOp.InvokeOp invokeOp, Map<Value, Object> capturedValues) {
        return switch (invokeOp.invokeDescriptor().refType()) {
            case ClassType classType -> {
                if (classType.toClassName().equals(Integer.class.getName())) {
                    yield IntegerStaticTransformNode.newIntTransformNode(invokeOp, capturedValues);
                } else if (classType.toClassName().equals(Long.class.getName())) {
                    yield LongStaticTransformNode.newLongTransformNode(invokeOp, capturedValues);
                } else if (classType.toClassName().equals(Short.class.getName())
                    || classType.toClassName().equals(Byte.class.getName())) {
                    yield ShortByteStaticTransformNode.newShortByteTransformNode(invokeOp, capturedValues);
                }
                yield EvaluableStaticTransformNode.newEvaluableStaticTransformNode(invokeOp, capturedValues);
            }
            default -> throw new IllegalArgumentException("Unsupported field type");
        };
    }

    @Override
    public String getAsString() {
        return transformer != null ? transformer.transform() : Objects.requireNonNull(value).getValueAsString();
    }
}
