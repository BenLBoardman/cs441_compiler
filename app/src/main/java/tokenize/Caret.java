package tokenize;

public record Caret() implements Token {
    @Override public TokenType getType() { return TokenType.CARET; }
}
