package tokenize.token;

public record Locals() implements Token {
    @Override public TokenType getType() { return TokenType.LOCALS; }
}
