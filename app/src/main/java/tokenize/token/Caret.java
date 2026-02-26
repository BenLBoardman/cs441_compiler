package tokenize.token;

public record Caret() implements Token {
    @Override public TokenType getType() { return TokenType.CARET; }
}
