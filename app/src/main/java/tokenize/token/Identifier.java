package tokenize.token;

public record Identifier(String name) implements Token { @Override public TokenType getType() { return TokenType.IDENTIFIER; } }

