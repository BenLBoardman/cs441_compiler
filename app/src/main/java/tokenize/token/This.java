package tokenize.token;

public record This() implements Token {
    @Override public TokenType getType() { return TokenType.THIS; }
}
