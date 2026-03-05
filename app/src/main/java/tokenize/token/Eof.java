package tokenize.token;

public record Eof() implements Token {
    @Override public TokenType getType() { return TokenType.EOF; }
}
