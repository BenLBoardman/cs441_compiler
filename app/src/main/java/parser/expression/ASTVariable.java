package parser.expression;

import java.util.HashMap;

import parser.ASTClass;
import util.DataType;

public record ASTVariable(String name) implements ASTExpression {

    @Override
    public DataType getType(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) {
        return symbols.get(name);
    }


}
