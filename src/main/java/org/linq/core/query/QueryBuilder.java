package org.linq.core.query;

import java.util.LinkedHashSet;
import java.util.SequencedCollection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.linq.core.path.NameResolver;
import org.linq.core.path.PlainNameResolver;

class QueryBuilder<T> {

    private final NameResolver nameResolver;

    private final SequencedCollection<Condition> conditions;

    private final Class<T> clazz;

    public QueryBuilder(Class<T> clazz) {
        this.nameResolver = new PlainNameResolver();
        this.conditions = new LinkedHashSet<>();
        this.clazz = clazz;
    }

    public void addCondition(Condition condition) {
        conditions.add(condition);
    }

    public String build() {
        return "SELECT %s FROM %s %s".formatted("*", nameResolver.tableName(clazz), buildWhereClause());
    }

    private String buildWhereClause() {
        if (conditions.isEmpty()) {
            return "";
        }

        return " WHERE " + conditions.stream().map(Condition::toSql)
            .map(condition -> {
                var matcher = Pattern.compile("\\$\\w+").matcher(condition);
                while (matcher.find()) {
                    var group = matcher.group();
                    var fieldName = group.substring(1);
                    condition = condition.replace(group, nameResolver.columnName(fieldName));
                }
                return condition;
            }).collect(Collectors.joining(" AND "));
    }
}
