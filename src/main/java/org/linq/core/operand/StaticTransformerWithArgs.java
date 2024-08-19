package org.linq.core.operand;

public record StaticTransformerWithArgs(
    StaticTransformer transformer,
    Operand[] args
) {
    public String transform() {
        return transformer.transform(args);
    }
}