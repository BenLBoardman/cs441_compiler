package parser;

public record Binop(Expression lhs, String op, Expression rhs) implements Expression {
    public boolean isBool() {
        return op.equals("==") || op.equals("!=") || op.equals(">") || op.equals("<") || op.equals("<=") || op.equals(">=");
    }
}
