package parser.statement;

import java.util.HashMap;

import parser.ASTClass;
import parser.expression.ASTExpression;
import util.DataType;

public record ASTPrintStmt(ASTExpression str) implements ASTStatement{

    @Override
    public void checkTypes(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) {
        DataType strType = str.getType(types, symbols);
        if(!DataType.intType.equals(strType))
            throw new IllegalArgumentException("Error: print expects argument of type int, received "+strType);
    }}
