package tokenize.token;

public record LeftBrack() implements Token {
    @Override public TokenType getType() { return TokenType.LEFT_BRACK; }
}
