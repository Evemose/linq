package org.linq.core.condition;

import java.lang.reflect.code.Block;
import java.lang.reflect.code.Op;
import org.linq.core.operand.Operand;

class PlainCondition extends AbstractCondition {

    private final Operand operand;

    protected PlainCondition(Operand operand) {
        this.operand = operand;
    }

    static Condition ofSimpleBlock(Block block) {
        var op = ((Op.Result) block.terminatingOp().operands().getFirst()).op();
        return AbstractCondition.of(op);
    }

    static PlainCondition newPlainCondition(Op op) {
        return new PlainCondition(Operand.of(op, capturedValues.get()));
    }

    @Override
    public String toSql() {
        return operand.getAsString();
    }
}
