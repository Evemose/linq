package org.linq.core.operand;

class TransformerWithArgs {

    private final Transformer transformer;

    private final Object[] args;

    TransformerWithArgs(Transformer transformer, Object... args) {
        this.transformer = transformer;
        this.args = args;
    }

    public String transform(String fieldName) {
        return transformer.transform(fieldName, args);
    }
}
