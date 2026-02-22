package tokenize;

public record Dot() implements Token {
    @Override public TokenType getType() { return TokenType.DOT; }
}
