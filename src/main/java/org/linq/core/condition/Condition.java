package org.linq.core.condition;

import java.lang.reflect.code.Value;
import java.lang.reflect.code.op.CoreOp;
import java.util.Map;
import org.linq.core.query.QueryPart;

public interface Condition extends QueryPart {

    static Condition of(CoreOp.LambdaOp lambdaOp, Map<Value, Object> capturedValues) {
        return AbstractCondition.of(lambdaOp, capturedValues);
    }
}
