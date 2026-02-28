package parser.statement;

import java.util.HashMap;

import parser.ASTClass;
import parser.ASTMethod;
import parser.expression.ASTExpression;
import util.DataType;

public record ASTReturnStmt(ASTExpression output, ASTMethod method) implements ASTStatement{

    @Override
    public void checkTypes(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) {
        DataType returnType = output.getType(types, symbols);
        if(!returnType.equals(method.returnType()))
            throw new IllegalArgumentException("Type mismatch: Attempting to return "+returnType.typeName()+" from method returning "+method.returnType().typeName());
    }}
