package tokenize;

public record Locals() implements Token {
    @Override public TokenType getType() { return TokenType.LOCALS; }
}
