package org.linq.core.path;

public class PlainNameResolver implements NameResolver {

    @Override
    public String tableName(Class<?> clazz) {
        return toSnakeCase(clazz.getSimpleName());
    }

    private String toSnakeCase(String camelCase) {
        return camelCase.replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2")
            .replaceAll("([a-z])([A-Z])", "$1_$2")
            .toLowerCase();
    }

    @Override
    public String columnName(String fieldName) {
        return toSnakeCase(fieldName);
    }
}
