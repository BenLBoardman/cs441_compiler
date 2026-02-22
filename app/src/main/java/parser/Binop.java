package parser;

import java.util.HashMap;

import util.DataType;

public record Binop(Expression lhs, String op, Expression rhs) implements Expression {
    public boolean isBool() {
        return op.equals("==") || op.equals("!=") || op.equals(">") || op.equals("<") || op.equals("<=") || op.equals(">=");
    }

    @Override
    public DataType getType(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) {
        DataType lhType = lhs.getType(types, symbols);
        if(!lhType.equals(rhs.getType(types, symbols)) || lhType.isObject()) //types must be the same and must be ints
            throw new IllegalArgumentException("Error: both operants in binary operations must be the same type");
        return lhType;
    }
}
