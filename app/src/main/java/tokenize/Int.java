package tokenize;

public record Int() implements Token {
    @Override public TokenType getType() { return TokenType.INT; }
}
