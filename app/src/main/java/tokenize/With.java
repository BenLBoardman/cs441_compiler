package tokenize;

public record With() implements Token {
    @Override public TokenType getType() { return TokenType.WITH; }
}
