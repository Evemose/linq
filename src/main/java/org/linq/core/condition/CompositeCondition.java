package org.linq.core.condition;

import java.lang.reflect.code.op.ExtendedOp;

abstract class CompositeCondition extends AbstractCondition {

    protected final Condition left;
    protected final Condition right;
    protected final Operator operator;

    protected CompositeCondition(Condition left, Condition right, Operator operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    static CompositeCondition newCompositeOp(ExtendedOp.JavaConditionalOp op) {
        var conditions = extractConditions(op);
        switch (op) {
            case ExtendedOp.JavaConditionalAndOp _ -> {
                return new AndCondition(conditions.left, conditions.right);
            }
            case ExtendedOp.JavaConditionalOrOp _ -> {
                return new OrCondition(conditions.left, conditions.right);
            }
        }
    }

    private static LeftRightConditions extractConditions(ExtendedOp.JavaConditionalOp op) {
        var left = op.bodies().getFirst().blocks().getFirst();
        var right = op.bodies().getLast().blocks().getFirst();
        return new LeftRightConditions(PlainCondition.ofSimpleBlock(left), PlainCondition.ofSimpleBlock(right));
    }

    protected enum Operator {
        AND,
        OR
    }

    private record LeftRightConditions(Condition left, Condition right) {
    }

    private static final class AndCondition extends CompositeCondition {

        private AndCondition(Condition left, Condition right) {
            super(left, right, Operator.AND);
        }

        @Override
        public String toSql() {
            return left.toSql() + " AND " + right.toSql();
        }
    }

    private static final class OrCondition extends CompositeCondition {

        private OrCondition(Condition left, Condition right) {
            super(left, right, Operator.OR);
        }

        @Override
        public String toSql() {
            return "(" + left.toSql() + " OR " + right.toSql() + ")";
        }
    }
}
