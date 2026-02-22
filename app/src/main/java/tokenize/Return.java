package tokenize;

public record Return() implements Token {
    @Override public TokenType getType() { return TokenType.RETURN; }
}
