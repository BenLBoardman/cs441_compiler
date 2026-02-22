package parser;

import java.util.HashMap;

import javax.xml.crypto.Data;

import util.DataType;

public record Binop(Expression lhs, String op, Expression rhs) implements Expression {
    public boolean isBool() {
        return op.equals("==") || op.equals("!=") || op.equals(">") || op.equals("<") || op.equals("<=") || op.equals(">=");
    }

    @Override
    public DataType getType(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) {
        DataType lhType = lhs.getType(types, symbols);
        if(!lhType.equals(rhs.getType(types, symbols))) //types must be the same and must be ints
            throw new IllegalArgumentException("Error: both operands in binary operations must be the same type");
        else if(lhType.isObject() && !isBool())
            throw new IllegalArgumentException("Error: binary operands may only be objects for boolean operations");
        return DataType.intType; //either both sides are an int or this is an object boolean, which will return an int
    }

}
