package tokenize.token;

public record DollarSign() implements Token {

    @Override
    public TokenType getType() {
        return TokenType.DOLLAR_SIGN;
    }
    
}
