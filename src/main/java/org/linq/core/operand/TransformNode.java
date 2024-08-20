package org.linq.core.operand;

import java.lang.reflect.code.Op;
import java.lang.reflect.code.Value;
import java.lang.reflect.code.op.CoreOp;
import java.lang.reflect.code.type.ClassType;
import java.lang.reflect.code.type.PrimitiveType;
import java.util.Map;
import java.util.Objects;
import org.linq.core.util.Extracts;
import org.linq.core.util.Ops;

abstract class TransformNode implements Operand {

    private final TransformerWithArgs transform;

    private final Operand prevOperand;

    private final PlainValue plainVal;

    protected TransformNode(Operand prevOperand, TransformerWithArgs transform) {
        this.plainVal = null;
        this.transform = transform;
        this.prevOperand = prevOperand;
    }

    protected TransformNode(PlainValue plainVal) {
        this.plainVal = plainVal;
        this.transform = null;
        this.prevOperand = null;
    }

    public static Operand of(Op op, Map<Value, Object> capturedValues) {

        if (op instanceof CoreOp.VarAccessOp || op instanceof CoreOp.ConstantOp) {
            return fallback(op, capturedValues);
        }

        return switch (Ops.prevOp(op).orElseThrow().resultType()) {
            case ClassType classType -> {
                if (classType.toClassName().equals(String.class.getName())) {
                    yield StringTransformNode.newStringTransformNode(op, capturedValues);
                }
                yield fallback(op, capturedValues);
            }
            case PrimitiveType primitiveType -> {
                if (primitiveType == PrimitiveType.INT || primitiveType == PrimitiveType.LONG) {
                    yield NumberTransformNode.newIntTransformNode(op, capturedValues);
                }
                yield fallback(op, capturedValues);
            }
            default -> throw new IllegalArgumentException("Unsupported field type");
        };
    }

    private static TransformNode fallback(Op op, Map<Value, Object> capturedValues) {
        if (Ops.isColumnAccessor(op, capturedValues)) {
            return new TransformNode(new ColumnValue(Extracts.accessorToFieldName(op))) {};
        } else {
            return EvaluableTransformNode.newEvaluableTransformNode(op, capturedValues);
        }
    }

    @Override
    public String getAsString() {
        return prevOperand != null ?
            Objects.requireNonNull(transform).transform(prevOperand.getAsString()) :
            Objects.requireNonNull(plainVal).getValueAsString();
    }

}
