package org.linq.core.operand;

class StringLiteralValue extends LiteralValue<String> {

    public StringLiteralValue(String value) {
        super(value);
    }

    @Override
    public String getValueAsString() {
        return "'" + value + "'";
    }

    public static StringLiteralValue ofRaw(String value) {
        return new RawStringLiteralValue(value);
    }

    private static final class RawStringLiteralValue extends StringLiteralValue {

        public RawStringLiteralValue(String value) {
            super(value);
        }

        @Override
        public String getValueAsString() {
            return value;
        }
    }
}
