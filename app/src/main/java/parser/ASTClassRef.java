package parser;

import java.util.HashMap;

import util.DataType;

public record ASTClassRef(String classname) implements ASTExpression {

    @Override
    public DataType getType(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) {
        return types.get(classname).type();
    }}
