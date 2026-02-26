package tokenize.token;

public record Colon() implements Token {
    @Override public TokenType getType() { return TokenType.COLON; }
}
