package parser;

import java.util.ArrayList;
import java.util.HashMap;

import util.DataType;

public record WhileStmt(Expression cond, ArrayList<Statement> body) implements Statement{

    @Override
    public void checkTypes(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'checkTypes'");
    }}
