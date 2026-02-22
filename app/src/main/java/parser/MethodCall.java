package parser;

import java.util.List;

public record MethodCall(Expression base, String methodname, List<Expression> args) implements Expression {}
