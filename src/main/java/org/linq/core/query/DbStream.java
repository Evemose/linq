package org.linq.core.query;

import java.lang.reflect.code.op.CoreOp;
import java.sql.DriverManager;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.linq.core.condition.Condition;
import org.linq.core.function.QuotableFunction;
import org.linq.core.function.QuotablePredicate;

public class DbStream<T> {

    private final QueryBuilder<T> queryBuilder;

    private final Class<T> clazz;

    public DbStream(Class<T> clazz) {
        this.queryBuilder = new QueryBuilder<>(clazz);
        this.clazz = clazz;
    }

    public DbStream<T> filter(QuotablePredicate<? super T> predicate) {
        queryBuilder.addCondition(Condition.of((CoreOp.LambdaOp) predicate.quoted().op(), predicate.quoted().capturedValues()));
        return this;
    }

    public <R> Stream<R> map(QuotableFunction<? super T, ? extends R> function) {
        return Stream.empty();
    }

    public List<T> toList() {
        try (var connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/linq", "postgres", "123")) {
            var run = new QueryRunner();
            return run.query(connection, queryBuilder.build(), new BeanListHandler<>(clazz));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
