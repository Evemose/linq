package org.linq.core.util;

import java.lang.reflect.code.Op;
import java.lang.reflect.code.Value;
import java.lang.reflect.code.op.CoreOp;
import java.lang.reflect.code.type.ClassType;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Ops {

    private static final Set<String> boxedTypesClassNames = Set.of(
        Integer.class.getName(),
        Long.class.getName(),
        Double.class.getName(),
        Float.class.getName(),
        Byte.class.getName(),
        Short.class.getName(),
        Character.class.getName(),
        Boolean.class.getName()
    );

    private Ops() {
    }

    /**
     * Check if the given op is static
     * @param op either a CoreOp.InvokeOp or a CoreOp.FieldAccessOp
     * @return true if selected in a passed op member is static. If op is not a CoreOp.InvokeOp or a CoreOp.FieldAccessOp, return false
     */
    public static boolean isStatic(Op op) {
        return switch (op) {
            case CoreOp.InvokeOp invokeOp -> invokeOp.operands().size() == invokeOp.invokeDescriptor().type().parameterTypes().size();
            case CoreOp.FieldAccessOp fieldAccessOp -> fieldAccessOp.operands().isEmpty();
            case CoreOp.NewOp newOp -> newOp.constructorType().parameterTypes().size() == newOp.operands().size();
            default -> false;
        };
    }

    public static boolean isTerminal(Op op) {
        if (op instanceof CoreOp.InvokeOp
            || op instanceof CoreOp.FieldAccessOp
            || op instanceof CoreOp.NewOp) {
            return isStatic(op);
        }
        return true;
    }

    public static Optional<Op> prevOp(Op op) {
        if (op.operands().isEmpty()) {
            return Optional.empty();
        }

        return switch (op) {
            case CoreOp.NewOp _, CoreOp.InvokeOp _, CoreOp.FieldAccessOp _ -> {
                if (isStatic(op)) {
                    yield Optional.empty();
                }
                yield Optional.of(((Op.Result) op.operands().getFirst()).op());
            }
            default -> Optional.empty();
        };
    }

    public static boolean isColumnAccessor(Op op, Map<Value, Object> capturedValues) {
        var prevOp = prevOp(op);
        return prevOp.isPresent() &&
           prevOp.get() instanceof CoreOp.VarAccessOp varAccessOp && !capturedValues.containsKey(varAccessOp.result());
    }

    public static Op rootOp(Op op) {
        var currentOp = op;
        if (currentOp instanceof CoreOp.ConstantOp) {
            return currentOp;
        }
        while (!currentOp.operands().isEmpty()
            && currentOp.operands().getFirst() instanceof Op.Result result
            && (result.op() instanceof CoreOp.FieldAccessOp || result.op() instanceof CoreOp.InvokeOp)) {
            currentOp = result.op();
        }
        return currentOp;
    }

}
