package parser;

import java.util.ArrayList;
import java.util.HashMap;

import util.DataType;

public record ASTIfOnlyStmt(ASTExpression cond, ArrayList<ASTStatement> body) implements ASTStatement{

    @Override
    public void checkTypes(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) {
        DataType condType = cond.getType(types, symbols);
        if(condType.isObject())
            throw new IllegalArgumentException("If-Only conditions must have type int");
        for(ASTStatement s : body)
            s.checkTypes(types, symbols);
    }}
