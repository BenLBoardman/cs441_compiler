package parser;

import java.util.HashMap;

import util.DataType;

public record NullExpr(DataType type) implements Expression {

    @Override
    public DataType getType(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) {
        return type;
    }
}
