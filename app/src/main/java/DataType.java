import java.util.HashMap;

public record DataType(String typeName, boolean isObject) {
    private static HashMap<String, DataType> typeNames = new HashMap<>();

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
            return new DataType("int", false);
        else if (type.getType() == TokenType.IDENTIFIER)
            return new DataType(((Identifier) type).name(), true);
        else
            throw new IllegalArgumentException("Expected return type, found " + type);
    }
}
