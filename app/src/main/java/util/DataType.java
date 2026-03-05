package util;
import java.util.HashMap;

import tokenize.token.Identifier;
import tokenize.token.Token;
import tokenize.token.TokenType;

public record DataType(String typeName, boolean isObject) {
    private static HashMap<String, DataType> typeNames = new HashMap<>(); //list of all valid data types
    public static final DataType intType = new DataType("int", false);

    public static DataType getType(Token t) {
        DataType type = typeNames.get(t.toString());
        if(type == null) {
            type = processType(t);
            typeNames.put(t.toString(), type);
        }
        return type;
    }

    public static DataType processType(Token type) {
        if (type.getType() == TokenType.INT)
            return intType;
        else if (type.getType() == TokenType.IDENTIFIER)
            return new DataType(((Identifier) type).name(), true);
        else
            throw new IllegalArgumentException("Expected return type, found " + type);
    }

    @Override
    public boolean equals(Object o) {
        return this.typeName.equals(((DataType)o).typeName);
    }

    @Override
    public final String toString() {
        return typeName;
    }
}
