package org.linq.core.condition;

import java.lang.reflect.code.Op;
import java.lang.reflect.code.op.CoreOp;

class Negation extends AbstractCondition {

    private final Condition condition;

    private Negation(Condition condition) {
        this.condition = condition;
    }

    public static Negation newNegation(CoreOp.NotOp op) {
        return new Negation(AbstractCondition.of(((Op.Result) op.operands().getFirst()).op()));
    }

    @Override
    public String toSql() {
        return "NOT ( " + condition.toSql() + " )";
    }
}
