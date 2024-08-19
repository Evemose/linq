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

    protected PlainCondition(Operand field, List<Operand> values) {
        this.field = field;
        this.values = values;
    }

    static Condition ofSimpleBlock(Block block) {
        var op = ((Op.Result) block.terminatingOp().operands().getFirst()).op();
        return PlainCondition.newPlainCondition(op);
    }

    static PlainCondition newPlainCondition(Op op) {
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
            case ClassType classType when classType.toClassName().equals(String.class.getName()) ->
                StringCondition.newStringCondition(invokeOp, capturedValues.get());
            default -> ImmediatelyEvaluableCondition.newImmediatelyEvaluableCondition(invokeOp, capturedValues.get());
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
