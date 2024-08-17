package org.linq.core.operand;

import java.lang.reflect.code.Op;
import java.lang.reflect.code.Value;
import java.util.Map;
import org.linq.core.util.Values;

class LiteralValue<T> implements PlainValue {

    protected final T value;

    LiteralValue(T value) {
        this.value = value;
    }

    static <T> LiteralValue<T> ofResultOfOp(Op op, Map<Value, Object> capturedValues) {
        return new LiteralValue<>(Values.valueOf(op, capturedValues));
    }

    @Override
    public String getValueAsString() {
        return String.valueOf(value);
    }
}
