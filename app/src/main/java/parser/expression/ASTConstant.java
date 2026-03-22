package parser.expression;

import java.util.HashMap;

import cfg.BasicBlock;
import cfg.expr.CFGExpr;
import cfg.expr.data.CFGPrimitive;
import cfg.expr.data.CFGVar;
import parser.ASTClass;
import util.DataType;

public class ASTConstant extends ASTExpression {
    private long value;

    public ASTConstant(long value) {
        this.value = value;
    }

    @Override
    public DataType getType(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) {
        return DataType.intType;
    }

    @Override
    public CFGExpr toCFG(CFGVar assn, BasicBlock currBlock, boolean requireVal) {
        return CFGPrimitive.getPrimitive(value);
    }

    public long value() {
        return this.value;
    }
}
