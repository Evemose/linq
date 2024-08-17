package org.linq.core.operand;

import java.lang.reflect.code.Op;
import java.lang.reflect.code.TypeElement;
import java.lang.reflect.code.Value;
import java.lang.reflect.code.op.CoreOp;
import java.lang.reflect.code.type.ClassType;
import java.util.List;
import java.util.Map;

abstract class AbstractOperand implements Operand {

    private final List<TransformerWithArgs> transforms;

    private final PlainValue plainVal;

    protected AbstractOperand(PlainValue plainVal, List<TransformerWithArgs> transforms) {
        this.plainVal = plainVal;
        this.transforms = transforms;
    }

    public static Operand of(Op op, Map<Value, Object> capturedValues) {
        return switch (rootColumnType(op)) {
            case ClassType classType when classType.toClassName().equals(String.class.getName()) -> new StringOperand(op, capturedValues);
            default -> throw new IllegalArgumentException("Unsupported field type");
        };
    }

    private static TypeElement rootColumnType(Op op) {
        var rootOp = rootOp(op);
        if (rootOp instanceof CoreOp.FieldAccessOp fieldAccessOp) {
            return fieldAccessOp.fieldDescriptor().type();
        } else if (rootOp instanceof CoreOp.InvokeOp invokeOp) {
            return invokeOp.invokeDescriptor().type().returnType();
        } else {
            return rootOp.resultType();
        }
    }

    protected static Op rootOp(Op op) {
        var currentOp = op;
        if (currentOp instanceof CoreOp.ConstantOp) {
            return currentOp;
        }
        while (currentOp.operands().getFirst() instanceof Op.Result result && (result.op() instanceof CoreOp.FieldAccessOp || result.op() instanceof CoreOp.InvokeOp)) {
            currentOp = result.op();
        }
        return currentOp;
    }

    protected static String accessorToFieldName(String getter) {
        if (!getter.startsWith("get")) {
            return getter;
        }
        return getter.substring(3, 4).toLowerCase() + getter.substring(4);
    }

    public String getFieldAsString() {
        return transforms.stream().reduce(plainVal.getValueAsString(), (s, t) -> t.transform(s), (s1, _) -> s1);
    }

}
