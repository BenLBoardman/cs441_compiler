package util;
import java.util.HashMap;

import tokenize.token.Identifier;
import tokenize.token.Token;
import tokenize.token.TokenType;

public record DataType(String typeName, boolean isObject, boolean isArr) {

    public DataType(String typeName, boolean isObject) {
        this(typeName, isObject, false);
    }
    
    private static HashMap<String, DataType> typeNames = new HashMap<>(); //list of all valid data types
    public static final DataType intType = new DataType("int", false);
    public static final DataType errType = new DataType("err", false);
    public static final DataType ptrType = new DataType("ptr", true); //generic pointer object, used internally

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
            return errType;
    }

    @Override
    public boolean equals(Object o) {
        return this.typeName.equals(((DataType)o).typeName) && this.isArr == ((DataType)o).isArr();
    }

    public DataType noArray() {
        return new DataType(typeName, isObject);
    }

    @Override
    public final String toString() {
        return typeName;
    }
}
