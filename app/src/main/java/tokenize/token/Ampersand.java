package tokenize.token;

public record Ampersand() implements Token {
    @Override public TokenType getType() { return TokenType.AMPERSAND; }
}
