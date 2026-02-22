package parser;

import java.util.HashMap;

import util.DataType;

public record Constant(long value) implements Expression {

    @Override
    public DataType getType(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) {
        return DataType.intType;
    }
}
