package tokenize.token;

public record TClass() implements Token {
    @Override public TokenType getType() { return TokenType.CLASS; }
}
