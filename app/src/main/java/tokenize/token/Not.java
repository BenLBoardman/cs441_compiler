package tokenize.token;

public record Not() implements Token {
    @Override public TokenType getType() { return TokenType.NOT; }
}
