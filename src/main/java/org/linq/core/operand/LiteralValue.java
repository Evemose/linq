package org.linq.core.operand;

class LiteralValue<T> implements PlainValue {

    protected final T value;

    protected LiteralValue(T value) {
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    public static <T> LiteralValue<T> of(T value) {
        if (value instanceof String s) {
            return (LiteralValue<T>) new StringLiteralValue(s);
        } else {
            return new LiteralValue<>(value);
        }
    }

    @Override
    public String getValueAsString() {
        return String.valueOf(value);
    }
}
