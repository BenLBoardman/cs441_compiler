package tokenize.token;

public record While() implements Token {
    @Override public TokenType getType() { return TokenType.WHILE; }
}
