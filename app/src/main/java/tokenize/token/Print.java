package tokenize.token;

public record Print() implements Token {
    @Override public TokenType getType() { return TokenType.PRINT; }
}
