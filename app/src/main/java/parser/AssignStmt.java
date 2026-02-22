package parser;

import java.util.HashMap;

import util.DataType;

public record AssignStmt(Variable var, Expression rhs) implements Statement{

    @Override
    public void checkTypes(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) {
        DataType varType, rhType;
        varType = symbols.get(var.name());
        rhType = rhs.getType(types, symbols);
        if(!varType.equals(rhType))
            throw new IllegalArgumentException("Expected assignment of type "+varType.typeName()+" for var "+var.name()+", received "+rhType.typeName());
    }}
