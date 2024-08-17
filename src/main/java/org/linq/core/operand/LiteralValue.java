package org.linq.core.operand;

class LiteralValue<T> implements PlainValue {

    protected final T value;

    LiteralValue(T value) {
        this.value = value;
    }

    @Override
    public String getValueAsString() {
        return String.valueOf(value);
    }
}
