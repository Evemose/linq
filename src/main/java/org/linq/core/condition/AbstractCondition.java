package org.linq.core.condition;

import java.lang.reflect.code.Op;
import java.lang.reflect.code.Value;
import java.lang.reflect.code.op.CoreOp;
import java.lang.reflect.code.op.ExtendedOp;
import java.lang.reflect.code.type.PrimitiveType;
import java.util.Collections;
import java.util.Map;

public abstract class AbstractCondition implements Condition {

    protected static final ThreadLocal<Map<Value, Object>> capturedValues = ThreadLocal.withInitial(Map::of);

    protected AbstractCondition() {
    }

    public static Condition of(CoreOp.LambdaOp lambdaOp, Map<Value, Object> capturedValues) {
        if (!lambdaOp.invokableType().returnType().equals(PrimitiveType.BOOLEAN)) {
            throw new IllegalArgumentException("Condition must return boolean");
        }
        AbstractCondition.capturedValues.set(Collections.unmodifiableMap(capturedValues));
        return AbstractCondition.of(((Op.Result) lambdaOp.body().blocks().getFirst().ops().getLast().operands().getFirst()).op());
    }

    private static Condition of(Op op) {
        return op instanceof ExtendedOp.JavaConditionalOp conditionalOp ?
            CompositeCondition.newCompositeOp(conditionalOp) : PlainCondition.newPlainOp(op);
    }

}