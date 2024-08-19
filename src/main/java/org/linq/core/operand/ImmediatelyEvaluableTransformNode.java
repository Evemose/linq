package org.linq.core.operand;

import java.lang.reflect.code.Op;
import java.lang.reflect.code.Value;
import java.util.Map;
import org.linq.core.util.Values;

class ImmediatelyEvaluableTransformNode extends TransformNode {

    protected ImmediatelyEvaluableTransformNode(PlainValue plainVal) {
        super(plainVal);
    }

    public static ImmediatelyEvaluableTransformNode newImmediatelyEvaluableTransformNode(Op op, Map<Value, Object> capturedValues) {
        return new ImmediatelyEvaluableTransformNode(new LiteralValue<>(Values.valueOf(op, capturedValues)));
    }
}
