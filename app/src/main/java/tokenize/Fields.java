package tokenize;

public record Fields() implements Token {
    @Override public TokenType getType() { return TokenType.FIELDS; }
}
