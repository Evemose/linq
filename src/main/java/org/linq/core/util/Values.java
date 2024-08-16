package org.linq.core.util;

import java.lang.reflect.code.op.CoreOp;
import java.util.Map;

public class Values {

    private Values() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T valueOf(CoreOp.VarAccessOp varAccessOp, Map<java.lang.reflect.code.Value, Object> capturedValues) {
        var captured = capturedValues.get(varAccessOp.varOp().result());
        if (captured instanceof CoreOp.Var<?> var) {
            return (T) var.value();
        }
        throw new IllegalArgumentException("Value not found for " + varAccessOp);
    }
}
