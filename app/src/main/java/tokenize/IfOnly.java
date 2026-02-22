package tokenize;

public record IfOnly() implements Token {
    @Override public TokenType getType() { return TokenType.IFONLY; }
}
