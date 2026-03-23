package tokenize.token;

public record Array() implements Token {

    @Override
    public TokenType getType() {
        return TokenType.ARRAY;
    }
    
}
