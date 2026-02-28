package parser.statement;

import java.util.HashMap;

import parser.ASTClass;
import parser.expression.ASTExpression;
import parser.expression.ASTVariable;
import util.DataType;

public record ASTAssignStmt(ASTVariable var, ASTExpression rhs) implements ASTStatement{

    @Override
    public void checkTypes(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) {
        DataType varType, rhType;
        varType = symbols.get(var.name());
        rhType = rhs.getType(types, symbols);
        if(!varType.equals(rhType))
            throw new IllegalArgumentException("Expected assignment of type "+varType.typeName()+" for var "+var.name()+", received "+rhType.typeName());
    }}
