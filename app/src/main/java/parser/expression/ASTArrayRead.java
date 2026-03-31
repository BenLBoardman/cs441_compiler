package parser.expression;

import java.util.HashMap;

import cfg.BasicBlock;
import cfg.expr.CFGExpr;
import cfg.expr.CFGGet;
import cfg.expr.data.CFGValue;
import cfg.expr.data.CFGVar;
import cfg.op.CFGAssn;
import parser.ASTClass;
import util.DataType;

public class ASTArrayRead extends ASTExpression {
    private ASTExpression arr;
    private ASTExpression index;
    

    public ASTArrayRead(ASTExpression arr, ASTExpression index) {
        this.arr = arr;
        this.index = index;
    }


    @Override
    public DataType getType(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) {
        if(!index.getType(types, symbols).equals(DataType.intType))
            throw new IllegalArgumentException("Array index must be of int type");
        return arr.getType(types, symbols).noArray();
    }

    @Override
    public CFGExpr toCFG(CFGVar assn, BasicBlock currBlock, boolean requireVal) {
        CFGVar arr = (CFGVar)this.arr.toCFG(assn, currBlock, true);
        CFGValue index = (CFGValue)this.index.toCFG(assn, currBlock, true);
        CFGVar out = CFGVar.makeTmpVar(null);
        currBlock.addOp(new CFGAssn(out, new CFGGet(arr, index)));
        return out;
    }
    
}
