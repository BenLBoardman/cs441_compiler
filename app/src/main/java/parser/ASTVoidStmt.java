package parser;

import java.util.HashMap;

import util.DataType;

public record ASTVoidStmt(ASTExpression rhs) implements ASTStatement{

    @Override
    public void checkTypes(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) {
        rhs.getType(types, symbols);
    }}
