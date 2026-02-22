package parser;

public record FieldRead(Expression base, String fieldname) implements Expression {}
