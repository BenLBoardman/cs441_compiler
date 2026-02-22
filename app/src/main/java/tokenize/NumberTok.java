package tokenize; 

public record NumberTok(long value) implements Token { @Override public TokenType getType() { return TokenType.NUMBER; } }
