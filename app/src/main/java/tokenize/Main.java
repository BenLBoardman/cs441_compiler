package tokenize;

public record Main() implements Token {
    @Override public TokenType getType() { return TokenType.MAIN; }
}
