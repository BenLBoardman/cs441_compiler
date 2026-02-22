package parser;

import java.util.ArrayList;
import java.util.HashMap;

import util.DataType;

public record IfElseStmt(Expression cond, ArrayList<Statement> body, ArrayList<Statement> elseBody) implements Statement{

    @Override
    public void checkTypes(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) {
        DataType condType = cond.getType(types, symbols);
        if(condType.isObject())
            throw new IllegalArgumentException("If-Only conditions must have type int");
        for(Statement s : body)
            s.checkTypes(types, symbols);
        for(Statement s : elseBody)
            s.checkTypes(types, symbols);
    }}
