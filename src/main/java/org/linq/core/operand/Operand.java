package org.linq.core.operand;

import java.lang.reflect.code.Op;
import java.lang.reflect.code.Value;
import java.util.Map;

public interface Operand {

    static Operand of(Op op, Map<Value, Object> capturedValues) {
        return TransformNode.of(op, capturedValues);
    }

    String getAsString();

}
