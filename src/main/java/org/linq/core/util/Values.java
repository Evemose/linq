package org.linq.core.util;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.code.Op;
import java.lang.reflect.code.Value;
import java.lang.reflect.code.op.CoreOp;
import java.lang.reflect.code.type.ClassType;
import java.util.Map;
import org.linq.core.exceptions.UncapturedValueException;

public class Values {

    private Values() {
    }

    /**
     * Returns the captured value of the given op.
     * @param varAccessOp the var access op
     * @param capturedValues the captured values
     * @return the value contained in the var
     * @param <T> the type of the captured value, effectively type of the var value
     * @throws UncapturedValueException if value of the var is not captured
     */
    @SuppressWarnings("unchecked")
    public static <T> T valueOf(CoreOp.VarAccessOp varAccessOp, Map<Value, Object> capturedValues) {
        var captured = capturedValues.get(varAccessOp.varOp().result());

        if (captured == null) {
            throw new UncapturedValueException("Captured value not found for " + varAccessOp.varOp().varName());
        }

        if (captured instanceof CoreOp.Var<?> var) {
            return (T) var.value();
        }
        throw new IllegalArgumentException("Value not found for " + varAccessOp);
    }

    /**
     * Returns the captured value of the given op.
     * @param fieldAccessOp the field access op
     * @param capturedValues the captured values
     * @return the value contained in the field
     * @param <T> the type of the captured value, effectively type of the field value
     * @throws UncapturedValueException if any operand of op or operand of any nested op is not captured
     */
    @SuppressWarnings("unchecked")
    public static <T> T valueOf(CoreOp.FieldAccessOp fieldAccessOp, Map<Value, Object> capturedValues) {
        try {
            var fieldHandle = fieldAccessOp.fieldDescriptor().resolveToHandle(MethodHandles.lookup());
            var owner = Values.valueOf(((Op.Result) fieldAccessOp.operands().getFirst()).op(), capturedValues);
            return (T) fieldHandle.get(owner);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the captured value of the given op.
     * @param invokeOp the invoke op
     * @param capturedValues the captured values
     * @return the value returned by the method
     * @param <T> the type of the captured value
     * @throws UncapturedValueException if any operand of op or operand of any nested op is not captured
     */
    @SuppressWarnings("unchecked")
    public static <T> T valueOf(CoreOp.InvokeOp invokeOp, Map<Value, Object> capturedValues) {
        try {
            var methodHandle = invokeOp.invokeDescriptor().resolveToHandle(MethodHandles.lookup());
            var isStatic = invokeOp.operands().size() == invokeOp.invokeDescriptor().type().parameterTypes().size();
            var params = invokeOp.operands().stream()
                .skip(!isStatic ? 1 : 0)
                .map(result -> Values.valueOf(((Op.Result) result).op(), capturedValues))
                .toArray();
            if (!isStatic) {
                var owner = Values.valueOf(((Op.Result) invokeOp.operands().getFirst()).op(), capturedValues);
                methodHandle = methodHandle.bindTo(owner);
            }
            return (T) methodHandle.invokeWithArguments(params);
        } catch (UncapturedValueException e) {
            throw e;
        }
        catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the captured value of the given op.
     * @param newOp the new op
     * @param capturedValues the captured values
     * @return the value returned by constructor invocation
     * @param <T> the type of the captured value
     * @throws UncapturedValueException if any operand of op or operand of any nested op is not captured
     */
    @SuppressWarnings("unchecked")
    public static <T> T valueOf(CoreOp.NewOp newOp, Map<Value, Object> capturedValues) {
        try {
            var constructorClass = Class.forName(((ClassType) newOp.resultType()).toClassName());
            var paramTypes = newOp.constructorType().parameterTypes().stream()
                .map(type -> {
                    try {
                        return Class.forName(((ClassType) type).toClassName());
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }).toArray(Class[]::new);

            var constructorHandle = MethodHandles.lookup().findConstructor(constructorClass, MethodType.methodType(void.class, paramTypes));

            var params = newOp.operands().stream()
                .map(result -> Values.valueOf(((Op.Result) result).op(), capturedValues))
                .toArray();

            return (T) constructorHandle.invokeWithArguments(params);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the captured value of the given op.
     * @param op the op
     * @param capturedValues the captured values
     * @return the captured value
     * @param <T> the type of the captured value
     * @throws UncapturedValueException if any operand of op or operand of any nested op is not captured
     */
    @SuppressWarnings("unchecked")
    public static <T> T valueOf(Op op, Map<Value, Object> capturedValues) {
        return switch (op) {
            case CoreOp.VarAccessOp varAccessOp -> valueOf(varAccessOp, capturedValues);
            case CoreOp.FieldAccessOp fieldAccessOp -> valueOf(fieldAccessOp, capturedValues);
            case CoreOp.ConstantOp constantOp -> (T) constantOp.value();
            case CoreOp.InvokeOp invokeOp -> valueOf(invokeOp, capturedValues);
            case CoreOp.NewOp newOp -> valueOf(newOp, capturedValues);
            default -> throw new IllegalArgumentException("Unsupported op " + op);
        };
    }
}
