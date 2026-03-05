package parser.expression;

import java.util.HashMap;

import parser.ASTClass;
import util.DataType;

public record ASTConstant(long value) implements ASTExpression {

    @Override
    public DataType getType(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) {
        return DataType.intType;
    }
}
