package tokenize.token;

public record AtSign() implements Token {
    @Override public TokenType getType() { return TokenType.ATSIGN; }
}
