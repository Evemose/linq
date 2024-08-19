package org.linq.core.condition;

import java.lang.reflect.code.Op;
import java.lang.reflect.code.Value;
import java.lang.reflect.code.op.CoreOp;
import java.lang.reflect.code.type.MethodRef;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.linq.core.operand.Operand;
import org.linq.core.util.Operands;

class StringCondition extends PlainCondition {

    private final SqlOp sqlOp;

    StringCondition(Operand field, List<Operand> values, SqlOp sqlOp) {
        super(field, values);
        this.sqlOp = sqlOp;
    }

    static StringCondition newStringCondition(CoreOp.InvokeOp invokeOp, Map<Value, Object> capturedValues) {
        return new StringCondition(
            Operand.of(((Op.Result) invokeOp.operands().getFirst()).op(), capturedValues),
            Operands.paramsOf(invokeOp, capturedValues),
            SqlOp.ofInvokeDescriptor(invokeOp.invokeDescriptor())
        );
    }

    @Override
    public String toSqlInner() {
        var result = sqlOp.sql().replace("!var", field.getAsString());
        if (sqlOp.sql().contains("!val")) {
            result = result.replace("!val", sqlOp.postProcessVal(values.getFirst().getAsString()));
        }
        return result;
    }

    private enum SqlOp {
        EQUALS("!var = !val"),
        CONTAINS("!var LIKE !val", value -> "CONCAT('%', " + value + ", '%')"),
        STARTS_WITH("!var LIKE !val", value -> "CONCAT(" + value + ", '%')"),
        ENDS_WITH("!var LIKE !val", value -> "CONCAT('%', " + value + ")"),
        // TODO postprocess regex more precisely
        MATCHES("!var SIMILAR TO !val", value ->  value.replaceAll("(?<!\\\\)\\.\\*", "%")),
        EMPTY("!var = ''"),
        BLANK("LTRIM(!var) = ''");


        private final String sql;

        private final Function<String, String> postProcess;

        SqlOp(String sql) {
            this(sql, Function.identity());
        }

        SqlOp(String sql, Function<String, String> postProcess) {
            this.sql = sql;
            this.postProcess = postProcess;
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

        public String sql() {
            return sql;
        }

        public String postProcessVal(String value) {
            return postProcess.apply(value);
        }
    }
}
