package org.linq.core.operand;

class TransformerWithArgs {

    private final Transformer transformer;

    private final Operand[] args;

    TransformerWithArgs(Transformer transformer, Operand... args) {
        this.transformer = transformer;
        this.args = args;
    }

    public String transform(String fieldName) {
        return transformer.transform(fieldName, args);
    }
}
