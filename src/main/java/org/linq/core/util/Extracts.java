package org.linq.core.util;

import java.lang.reflect.code.Op;
import java.lang.reflect.code.op.CoreOp;

public class Extracts {

    private Extracts() {
    }

    /**
     * Convert a getter method name to a field name.
     * Behaviour is following:
     * - If the op is CoreOp.InvokeOp and name does not start with "get", return the name as is
     * - If the op is CoreOp.InvokeOp and name starts with "get", return the name that goes after "get" with the first letter in lower case
     * - If the op is not CoreOp.FieldAccessOp, return the name as is
     * @param op, either a CoreOp.InvokeOp or a CoreOp.FieldAccessOp
     * @throws IllegalArgumentException if the op is not a CoreOp.InvokeOp or a CoreOp.FieldAccessOp
     * @return a field name
     */
    public static String accessorToFieldName(Op op) {
        return switch (op) {
            case CoreOp.InvokeOp invokeOp -> {
                var name = invokeOp.invokeDescriptor().name();
                if (name.startsWith("get")) {
                    yield name.substring(3, 4).toLowerCase() + name.substring(4);
                }
                yield name;
            }
            case CoreOp.FieldAccessOp fieldAccessOp -> fieldAccessOp.fieldDescriptor().name();
            default -> throw new IllegalArgumentException("Unsupported field type");
        };
    }

}
