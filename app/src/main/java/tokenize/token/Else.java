package tokenize.token;

public record Else() implements Token {
    @Override public TokenType getType() { return TokenType.ELSE; }
}
