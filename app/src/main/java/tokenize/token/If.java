package tokenize.token;

public record If() implements Token {
    @Override public TokenType getType() { return TokenType.IF; }
}
