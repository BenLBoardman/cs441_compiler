package tokenize;

public record Comma() implements Token {
    @Override public TokenType getType() { return TokenType.COMMA; }
}
