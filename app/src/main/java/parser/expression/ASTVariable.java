package parser.expression;

import java.util.HashMap;

import cfg.BasicBlock;
import cfg.expr.CFGExpr;
import cfg.expr.data.CFGVar;
import parser.ASTClass;
import util.DataType;

public class ASTVariable extends ASTExpression {
    private String name;

    public ASTVariable(String name) {
        this.name = name;
    }

    @Override
    public DataType getType(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) {
        return symbols.get(name);
    }
    @Override
    public CFGExpr toCFG(CFGVar assn, BasicBlock currBlock, boolean requireVal) {
        CFGVar tmpVar = currBlock.getActive(name);
                if (tmpVar == null)
                    throw new IllegalArgumentException("Attempted to access nonexistent or uninitialized variable "
                            + name + " (expr " + this + ")");
                return tmpVar;
    }

    public String name() {
        return this.name;
    }

}
