package org.linq.core.path;

public interface NameResolver {
    String tableName(Class<?> clazz);

    String columnName(String fieldName);
}
