package tokenize;

public record Colon() implements Token {
    @Override public TokenType getType() { return TokenType.COLON; }
}
