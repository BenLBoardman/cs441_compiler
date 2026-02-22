package tokenize;

public record Eof() implements Token {
    @Override public TokenType getType() { return TokenType.EOF; }
}
