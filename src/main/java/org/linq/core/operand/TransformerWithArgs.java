package org.linq.core.operand;

record TransformerWithArgs(
    Transformer transformer,
    Operand... args
) {
    public String transform(String fieldName) {
        return transformer.transform(fieldName, args);
    }
}
