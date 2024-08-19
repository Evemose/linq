package org.linq.core.operand;

import java.lang.reflect.code.Op;
import java.lang.reflect.code.Value;
import java.util.Map;
import org.linq.core.util.Values;

class ImmediatelyEvaluableStaticTransformNode extends StaticTransformNode {

    protected ImmediatelyEvaluableStaticTransformNode(PlainValue value) {
        super(value);
    }

    public static ImmediatelyEvaluableStaticTransformNode newEvaluableStaticTransformNode(Op op, Map<Value, Object> capturedValues) {
        return new ImmediatelyEvaluableStaticTransformNode(new LiteralValue<>(Values.valueOf(op, capturedValues)));
    }
}
