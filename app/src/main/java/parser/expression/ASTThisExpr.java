package parser.expression;

import java.util.HashMap;

import cfg.BasicBlock;
import cfg.expr.CFGExpr;
import cfg.expr.data.CFGVar;
import parser.ASTClass;
import util.DataType;

public class ASTThisExpr extends ASTExpression {
    private String classname;

    public ASTThisExpr(String classname) {
        this.classname = classname;
    }

    @Override
    public DataType getType(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) {
        return types.get(classname).type();
    }

    public String classname() {
        return classname;
    }

    @Override
    public CFGExpr toCFG(CFGVar assn, BasicBlock currBlock, boolean requireVal) {
        return currBlock.getActive("this");
    }
}
