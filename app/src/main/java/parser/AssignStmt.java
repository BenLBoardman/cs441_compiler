package parser;

public record AssignStmt(Variable var, Expression rhs) implements Statement{}
