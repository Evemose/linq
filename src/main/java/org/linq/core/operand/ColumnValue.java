package org.linq.core.operand;

record ColumnValue(String fieldName) implements PlainValue {

    @Override
    public String getValueAsString() {
        return "$" + fieldName;
    }
}
