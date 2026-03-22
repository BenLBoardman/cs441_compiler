package parser.expression;

import java.util.HashMap;

import cfg.BasicBlock;
import cfg.expr.CFGExpr;
import cfg.expr.data.CFGVar;
import parser.ASTClass;
import util.DataType;

public abstract class ASTExpression {
    public abstract DataType getType(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols);

    public abstract CFGExpr toCFG(CFGVar assn, BasicBlock currBlock, boolean requireVal);
}
