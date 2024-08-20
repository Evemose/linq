package org.linq.core.operand;

final class StringLiteralValue extends LiteralValue<String> {

    public StringLiteralValue(String value) {
        super(value);
    }

    @Override
    public String getValueAsString() {
        return "'" + value + "'";
    }
}
