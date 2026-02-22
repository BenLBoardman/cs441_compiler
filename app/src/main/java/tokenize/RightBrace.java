package tokenize;

public record RightBrace() implements Token {
    @Override public TokenType getType() { return TokenType.RIGHT_BRACE; }
}
