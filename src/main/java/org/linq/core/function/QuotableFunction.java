package org.linq.core.function;

import java.lang.reflect.code.Quotable;
import java.util.function.Function;

public interface QuotableFunction<T, R> extends Function<T, R>, Quotable {
}
