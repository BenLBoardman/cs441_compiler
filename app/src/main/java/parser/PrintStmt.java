package parser;

import java.util.HashMap;

import util.DataType;

public record PrintStmt(Expression str) implements Statement{

    @Override
    public void checkTypes(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) {
        DataType strType = str.getType(types, symbols);
        if(!DataType.intType.equals(strType))
            throw new IllegalArgumentException("Error: print expects argument of type int, received "+strType);
    }}
