package org.linq.core.condition;

import java.lang.reflect.code.Op;
import java.lang.reflect.code.Value;
import java.util.List;
import java.util.Map;
import org.linq.core.operand.Operand;

public class ImmediatelyEvaluableCondition extends PlainCondition {

    protected ImmediatelyEvaluableCondition(Operand operand) {
        super(operand, List.of());
    }

    public static ImmediatelyEvaluableCondition newImmediatelyEvaluableCondition(Op op, Map<Value, Object> capturedValues) {
        return new ImmediatelyEvaluableCondition(Operand.of(op, capturedValues));
    }

    @Override
    protected String toSqlInner() {
        return field.getAsString();
    }
}
