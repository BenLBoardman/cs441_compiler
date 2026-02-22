package tokenize;

public record RightBrack() implements Token {
    @Override public TokenType getType() { return TokenType.RIGHT_BRACK; }
}
