package tokenize;

public record RightParen() implements Token { @Override public TokenType getType() { return TokenType.RIGHT_PAREN; } }
