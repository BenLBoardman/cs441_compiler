package tokenize;

public record If() implements Token {
    @Override public TokenType getType() { return TokenType.IF; }
}
