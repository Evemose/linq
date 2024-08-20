package org.linq.core.operand;

import java.lang.reflect.code.Op;
import java.lang.reflect.code.Value;
import java.util.Map;
import org.linq.core.util.Values;

class EvaluableTransformNode extends TransformNode {

    protected EvaluableTransformNode(PlainValue plainVal) {
        super(plainVal);
    }

    public static EvaluableTransformNode newEvaluableTransformNode(Op op, Map<Value, Object> capturedValues) {
        return new EvaluableTransformNode(LiteralValue.of(Values.valueOf(op, capturedValues)));
    }
}
