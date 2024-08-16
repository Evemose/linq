package org.linq.core.query;

import java.lang.reflect.code.Block;
import java.lang.reflect.code.Op;
import java.lang.reflect.code.op.CoreOp;
import java.lang.reflect.code.op.ExtendedOp;
import java.lang.reflect.code.type.ClassType;
import java.lang.reflect.code.type.MethodRef;
import java.lang.reflect.code.type.PrimitiveType;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import org.linq.core.util.Values;

abstract class Condition implements QueryPart {

    protected static final ThreadLocal<Map<java.lang.reflect.code.Value, Object>> capturedValues = ThreadLocal.withInitial(Map::of);

    protected Condition() {
    }

    public static Condition of(CoreOp.LambdaOp lambdaOp, Map<java.lang.reflect.code.Value, Object> capturedValues) {
        if (!lambdaOp.invokableType().returnType().equals(PrimitiveType.BOOLEAN)) {
            throw new IllegalArgumentException("Condition must return boolean");
        }
        Condition.capturedValues.set(Collections.unmodifiableMap(capturedValues));
        return Condition.of(((Op.Result) lambdaOp.body().blocks().getFirst().ops().getLast().operands().getFirst()).op());
    }

    private static Condition of(Op op) {
        return op instanceof ExtendedOp.JavaConditionalOp conditionalOp ?
            CompositeCondition.newCompositeOp(conditionalOp) : PlainCondition.newPlainOp(op);
    }

    private static abstract class CompositeCondition extends Condition {

        protected final Condition left;
        protected final Condition right;
        protected final Operator operator;

        protected CompositeCondition(Condition left, Condition right, Operator operator) {
            this.left = left;
            this.right = right;
            this.operator = operator;
        }

        private static CompositeCondition newCompositeOp(ExtendedOp.JavaConditionalOp op) {
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

        private record LeftRightConditions(Condition left, Condition right) {
        }

        private enum Operator {
            AND,
            OR
        }

        private static class AndCondition extends CompositeCondition {

            private AndCondition(Condition left, Condition right) {
                super(left, right, Operator.AND);
            }

            @Override
            public String toSql() {
                return left.toSql() + " AND " + right.toSql();
            }
        }

        private static class OrCondition extends CompositeCondition {

            private OrCondition(Condition left, Condition right) {
                super(left, right, Operator.OR);
            }

            @Override
            public String toSql() {
                return "(" + left.toSql() + " OR " + right.toSql() + ")";
            }
        }
    }

    private static abstract class PlainCondition extends Condition {

        private boolean negated = false;

        protected final Operand field;

        private PlainCondition(Op op) {
            this.field = Operand.of(((Op.Result) op.operands().getFirst()).op(), capturedValues.get());
        }

        public void negate() {
            negated = true;
        }

        private static PlainCondition newPlainOp(Op op) {
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

        private static Condition ofSimpleBlock(Block block) {
            var op = ((Op.Result) block.terminatingOp().operands().getFirst()).op();
            return PlainCondition.newPlainOp(op);
        }

        @Override
        public final String toSql() {
            var sql = negated ? "NOT " : "";
            return sql + toSqlInner();
        }

        protected abstract String toSqlInner();
    }

    private static class StringCondition extends PlainCondition {

        private final SqlOp sqlOp;
        private final String value;

        private StringCondition(CoreOp.InvokeOp invokeOp) {
            super(invokeOp);
            this.sqlOp = SqlOp.ofInvokeDescriptor(invokeOp.invokeDescriptor());
            if (invokeOp.operands().size() == 1) {
                this.value = "";
            } else {
                var param = ((Op.Result) invokeOp.operands().getLast()).op();
                this.value = switch (param) {
                    case CoreOp.ConstantOp constant -> constant.value().toString();
                    case CoreOp.VarAccessOp varAccess -> Values.valueOf(varAccess, capturedValues.get());
                    default -> throw new IllegalArgumentException("Unsupported string condition");
                };
            }
        }

        @Override
        public String toSqlInner() {
            return sqlOp.sql().replace("$val", sqlOp.postProcess(value)).replace("$var", field.getFieldAsString());
        }

        private enum SqlOp {
            EQUALS("$var = '$val'"),
            CONTAINS("$var LIKE '%$val%'"),
            STARTS_WITH("$var LIKE '$val%'"),
            ENDS_WITH("$var LIKE '%$val'"),
            MATCHES("$var SIMILAR TO '$val'", SqlOp::processRegex),
            EMPTY("$var = ''"),
            BLANK("LTRIM($var) = ''");


            private final String sql;

            private final Function<String, String> postProcess;

            SqlOp(String sql) {
                this(sql, Function.identity());
            }

            SqlOp(String sql, Function<String, String> postProcess) {
                this.sql = sql;
                this.postProcess = postProcess;
            }

            public String sql() {
                return sql;
            }

            public static SqlOp ofInvokeDescriptor(MethodRef invokeDescriptor) {
                return switch (invokeDescriptor.name()) {
                    case "equals", "equalsIgnoreCase" -> EQUALS;
                    case "contains" -> CONTAINS;
                    case "startsWith" -> STARTS_WITH;
                    case "endsWith" -> ENDS_WITH;
                    case "matches" -> MATCHES;
                    case "isBlank" -> BLANK;
                    case "isEmpty" -> EMPTY;
                    default -> throw new IllegalArgumentException("Unsupported string condition");
                };
            }

            public String postProcess(String value) {
                return postProcess.apply(value);
            }

            private static String processRegex(String value) {
                return value.replace(".*", "%");
            }
        }
    }
}