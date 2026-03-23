package parser.statement;

import java.util.HashMap;

import parser.ASTClass;
import parser.expression.ASTExpression;
import util.DataType;

public non-sealed class ASTArrayWrite implements ASTStatement {
    private ASTExpression name;
    private ASTExpression index;
    private ASTExpression rhs;

    public ASTArrayWrite(ASTExpression name, ASTExpression index, ASTExpression rhs) {
        this.name = name;
        this.index = index;
        this.rhs = rhs;
    }

    @Override
    public void checkTypes(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) { //lhs and rhs types must match, index must have int type
        //TODO
    }

    public ASTExpression name() {
        return name;
    }

    public ASTExpression index() {
        return index;
    }
    
    public ASTExpression rhs() {
        return rhs;
    }
}
