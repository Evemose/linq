package org.linq.core.util;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.code.Op;
import java.lang.reflect.code.Value;
import java.lang.reflect.code.op.CoreOp;
import java.lang.reflect.code.type.ClassType;
import java.util.Map;

public class Values {

    private Values() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T valueOf(CoreOp.VarAccessOp varAccessOp, Map<Value, Object> capturedValues) {
        var captured = capturedValues.get(varAccessOp.varOp().result());
        if (captured instanceof CoreOp.Var<?> var) {
            return (T) var.value();
        }
        throw new IllegalArgumentException("Value not found for " + varAccessOp);
    }

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
                return (T) methodHandle.invokeWithArguments(params);
            } else {
                return (T) methodHandle.invokeWithArguments(params);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

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
