package parser.statement;

import java.util.HashMap;

import parser.ASTClass;
import parser.ASTMethod;
import parser.expression.ASTExpression;
import util.DataType;
import util.error.ErrorAccumulator;
import util.error.ReturnMismatchError;

public record ASTReturnStmt(ASTExpression output, ASTMethod method) implements ASTStatement{

    @Override
    public void checkTypes(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) {
        DataType returnType = output.getType(types, symbols);
        if(!returnType.equals(method.returnType()))
            ErrorAccumulator.addError(new ReturnMismatchError(0, method, output.getType(types, symbols)));
    }}
