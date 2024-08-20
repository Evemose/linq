package org.linq.core.operand;

import java.lang.reflect.code.Op;
import java.lang.reflect.code.Value;
import java.util.Map;
import org.linq.core.util.Values;

class EvaluableStaticTransformNode extends StaticTransformNode {

    protected EvaluableStaticTransformNode(PlainValue value) {
        super(value);
    }

    public static EvaluableStaticTransformNode newEvaluableStaticTransformNode(Op op, Map<Value, Object> capturedValues) {
        return new EvaluableStaticTransformNode(LiteralValue.of(Values.valueOf(op, capturedValues)));
    }
}
