package tokenize.token;

public record NullTok() implements Token {
    @Override public TokenType getType() { return TokenType.NULL; }
}
