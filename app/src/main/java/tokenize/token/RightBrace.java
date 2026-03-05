package tokenize.token;

public record RightBrace() implements Token {
    @Override public TokenType getType() { return TokenType.RIGHT_BRACE; }
}
