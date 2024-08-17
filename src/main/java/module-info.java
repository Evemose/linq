module core {
    requires java.compiler;
    requires org.apache.commons.dbutils;
    requires java.sql;
    exports org.linq.core;
    exports org.linq.core.function;
}