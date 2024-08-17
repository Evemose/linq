package org.linq.core.util;

import java.lang.reflect.code.Op;
import java.lang.reflect.code.Value;
import java.lang.reflect.code.op.CoreOp;
import java.util.List;
import java.util.Map;
import org.linq.core.operand.Operand;

public class Operands {

    private Operands() {
    }

    public static List<Operand> paramsOf(CoreOp.InvokeOp invokeOp, Map<Value, Object> capturedValues) {
        var isStatic = invokeOp.operands().size() == invokeOp.invokeDescriptor().type().parameterTypes().size();
        return invokeOp.operands().stream()
            .skip(!isStatic ? 1 : 0)
            .map(result -> Operand.of(((Op.Result) result).op(), capturedValues))
            .toList();
    }
}
