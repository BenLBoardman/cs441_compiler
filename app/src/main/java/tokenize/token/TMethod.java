package tokenize.token;

public record TMethod() implements Token {
    @Override public TokenType getType() { return TokenType.METHOD; }
}
