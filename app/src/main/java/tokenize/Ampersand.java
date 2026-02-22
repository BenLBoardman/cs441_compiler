package tokenize;

public record Ampersand() implements Token {
    @Override public TokenType getType() { return TokenType.AMPERSAND; }
}
