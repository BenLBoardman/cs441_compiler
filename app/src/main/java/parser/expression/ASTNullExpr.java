package parser.expression;

import java.util.HashMap;

import cfg.BasicBlock;
import cfg.expr.CFGExpr;
import cfg.expr.data.CFGPrimitive;
import cfg.expr.data.CFGVar;
import parser.ASTClass;
import util.DataType;

public class ASTNullExpr extends ASTExpression {
    private DataType type;

    public ASTNullExpr(DataType type) {
        this.type = type;
    }

    @Override
    public DataType getType(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) {
        return type;
    }

    public DataType type() {
        return this.type;
    }

    @Override
    public CFGExpr toCFG(CFGVar assn, BasicBlock currBlock, boolean requireVal) {
                return CFGPrimitive.getPrimitive(0);
            }
}
