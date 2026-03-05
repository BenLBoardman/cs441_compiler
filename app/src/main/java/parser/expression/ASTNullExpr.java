package parser.expression;

import java.util.HashMap;

import parser.ASTClass;
import util.DataType;

public record ASTNullExpr(DataType type) implements ASTExpression {

    @Override
    public DataType getType(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) {
        return type;
    }
}
