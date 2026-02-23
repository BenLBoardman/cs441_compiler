package parser;

import java.util.HashMap;

import util.DataType;

public record ASTConstant(long value) implements ASTExpression {

    @Override
    public DataType getType(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) {
        return DataType.intType;
    }
}
