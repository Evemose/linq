package org.linq.core.condition;

import java.lang.reflect.code.Op;
import java.lang.reflect.code.op.CoreOp;
import org.linq.core.operand.Operand;

abstract class Comparison extends AbstractCondition {

    private final Operand left;
    private final Operand right;
    private final String operator;

    protected Comparison(Operand left, Operand right, String operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    @Override
    public String toSql() {
        return "( " + left.getAsString() + " " + operator + " " + right.getAsString() + " )";
    }

    public static Comparison newComparison(Op op) {
        if (op.operands().size() != 2) {
            throw new IllegalArgumentException("Unsupported operation");
        }
        var left = Operand.of(((Op.Result) op.operands().getFirst()).op(), capturedValues.get());
        var right = Operand.of(((Op.Result) op.operands().getLast()).op(), capturedValues.get());
        return switch (op) {
            case CoreOp.GtOp _ -> new GreaterThan(left, right);
            case CoreOp.GeOp _ -> new GreaterThanOrEqual(left, right);
            case CoreOp.LtOp _ -> new LessThan(left, right);
            case CoreOp.LeOp _ -> new LessThanOrEqual(left, right);
            case CoreOp.EqOp _ -> new Equal(left, right);
            case CoreOp.NeqOp _ -> new NotEqual(left, right);
            default -> throw new IllegalArgumentException("Unsupported operation");
        };
    }

    private static final class GreaterThan extends Comparison {

        private GreaterThan(Operand left, Operand right) {
            super(left, right, ">");
        }
    }

    private static final class GreaterThanOrEqual extends Comparison {

        private GreaterThanOrEqual(Operand left, Operand right) {
            super(left, right, ">=");
        }
    }

    private static final class LessThan extends Comparison {

        private LessThan(Operand left, Operand right) {
            super(left, right, "<");
        }
    }

    private static final class LessThanOrEqual extends Comparison {

        private LessThanOrEqual(Operand left, Operand right) {
            super(left, right, "<=");
        }
    }

    private static final class Equal extends Comparison {

        private Equal(Operand left, Operand right) {
            super(left, right, "=");
        }
    }

    private static final class NotEqual extends Comparison {

        private NotEqual(Operand left, Operand right) {
            super(left, right, "<>");
        }
    }

}
