package tokenize;

public record Underscore() implements Token {
    @Override public TokenType getType() { return TokenType.UNDERSCORE; }
}
