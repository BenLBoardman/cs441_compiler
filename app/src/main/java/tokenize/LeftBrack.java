package tokenize;

public record LeftBrack() implements Token {
    @Override public TokenType getType() { return TokenType.LEFT_BRACK; }
}
