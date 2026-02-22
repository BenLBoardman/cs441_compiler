package tokenize;

public record TMethod() implements Token {
    @Override public TokenType getType() { return TokenType.METHOD; }
}
