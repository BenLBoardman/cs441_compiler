package tokenize.token;

public record LeftParen() implements Token { @Override public TokenType getType() { return TokenType.LEFT_PAREN; } }
