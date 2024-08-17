package org.linq.core.condition;

import java.lang.reflect.code.Block;
import java.lang.reflect.code.Op;
import java.lang.reflect.code.op.CoreOp;
import java.lang.reflect.code.type.ClassType;
import java.util.List;
import org.linq.core.operand.Operand;

abstract class PlainCondition extends AbstractCondition {

    protected final Operand field;
    protected final List<Operand> values;
    private boolean negated = false;

    protected PlainCondition(Op op, List<Operand> values) {
        this.field = Operand.of(((Op.Result) op.operands().getFirst()).op(), capturedValues.get());
        this.values = values;
    }

    static Condition ofSimpleBlock(Block block) {
        var op = ((Op.Result) block.terminatingOp().operands().getFirst()).op();
        return PlainCondition.newPlainOp(op);
    }

    static PlainCondition newPlainOp(Op op) {
        var negateOnCreate = false;
        var invokeOp = switch (op) {
            case CoreOp.InvokeOp invokeOp1 -> invokeOp1;
            case CoreOp.NotOp notOp -> {
                negateOnCreate = true;
                yield (CoreOp.InvokeOp) ((Op.Result) notOp.operands().getFirst()).op();
            }
            default -> throw new IllegalArgumentException("Unsupported condition type");
        };

        var painOp = switch (invokeOp.invokeDescriptor().refType()) {
            case ClassType classType when classType.toClassName().equals(String.class.getName()) -> new StringCondition(invokeOp);
            default -> throw new IllegalArgumentException("Unsupported condition type");
        };

        if (negateOnCreate) {
            painOp.negate();
        }
        return painOp;
    }

    public void negate() {
        negated = true;
    }

    @Override
    public final String toSql() {
        var sql = negated ? "NOT " : "";
        return sql + toSqlInner();
    }

    protected abstract String toSqlInner();
}
