package tokenize.token;

public record Returning() implements Token {
    @Override public TokenType getType() { return TokenType.RETURNING; }
}
