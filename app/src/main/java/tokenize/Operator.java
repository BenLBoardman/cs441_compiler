package tokenize;

public record Operator(String op) implements Token {
    @Override public TokenType getType() { return TokenType.OPERATOR; }
    public String getOp() { return this.op; }
}
