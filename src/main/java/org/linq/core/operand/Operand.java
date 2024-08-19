package org.linq.core.operand;

import java.lang.reflect.code.Op;
import java.lang.reflect.code.Value;
import java.lang.reflect.code.op.CoreOp;
import java.util.Map;
import org.linq.core.util.Ops;

public interface Operand {

    static Operand of(Op op, Map<Value, Object> capturedValues) {
        return op instanceof CoreOp.InvokeOp invokeOp && Ops.isStatic(invokeOp) ? StaticTransformNode.of(invokeOp, capturedValues)
            : TransformNode.of(op, capturedValues);
    }

    String getAsString();

}
