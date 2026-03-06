package parser.expression;

import java.util.HashMap;

import parser.ASTClass;
import util.DataType;
import util.error.BinopMismatchError;
import util.error.ErrorAccumulator;

public record ASTBinop(ASTExpression lhs, String op, ASTExpression rhs) implements ASTExpression {
    public boolean isBool() {
        return op.equals("==") || op.equals("!=") || op.equals(">") || op.equals("<") || op.equals("<=") || op.equals(">=");
    }

    @Override
    public DataType getType(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) {
        DataType lhType = lhs.getType(types, symbols);
        if(!lhType.equals(rhs.getType(types, symbols))) //types must be the same and must be ints
            ErrorAccumulator.addError(new BinopMismatchError(0, this.lhs.getType(types, symbols), this.rhs.getType(types, symbols)));
        else if(lhType.isObject() && !isBool())
            throw new IllegalArgumentException("Error: binary operands may only be objects for boolean operations");
        return DataType.intType; //either both sides are an int or this is an object boolean, which will return an int
    }

}
