package org.linq.core.operand;

interface Transformer {
    String transform(String fieldName, Operand... args);
}
