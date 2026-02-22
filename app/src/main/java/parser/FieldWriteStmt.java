package parser;

public record FieldWriteStmt(Expression base, String fieldname, Expression rhs) implements Statement{}
