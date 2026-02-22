package tokenize;

public record LeftBrace() implements Token {
    @Override public TokenType getType() { return TokenType.LEFT_BRACE; }
}
