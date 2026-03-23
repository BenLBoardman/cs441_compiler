package parser.expression;

import java.util.HashMap;

import cfg.BasicBlock;
import cfg.expr.CFGAlloc;
import cfg.expr.CFGBinOp;
import cfg.expr.CFGExpr;
import cfg.expr.data.CFGPrimitive;
import cfg.expr.data.CFGVar;
import cfg.op.CFGAssn;
import cfg.op.CFGSet;
import parser.ASTClass;
import util.DataType;

public class ASTArrayAlloc extends ASTExpression {
    private long size;
    private DataType type;

    public ASTArrayAlloc(long size, DataType type) {
        this.size = size;
        this.type = new DataType(type.typeName(), type.isObject(), true);
    }

    @Override
    public DataType getType(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) {
        DataType itemType =  types.get(this.type.typeName()).type();
        return itemType != null ? this.type : DataType.errType;
    }

    @Override
    public CFGExpr toCFG(CFGVar assn, BasicBlock currBlock, boolean requireVal) {
        CFGVar arrHead = CFGVar.makeTmpVar(DataType.ptrType);
        currBlock.addOp(new CFGAssn(arrHead, new CFGAlloc(CFGPrimitive.getPrimitive(size+3))));
        //build field map
        String bits = "0";
        if (this.type.isObject()) {
            for (int i = 0; i < this.size; i++) {
                bits = bits + "1";
            }
        }
        currBlock.addOp(new CFGSet(arrHead, CFGPrimitive.getPrimitive(2), CFGPrimitive.getPrimitive(Long.parseLong(bits, 2))));
        currBlock.addOp(new CFGAssn(assn, new CFGBinOp(arrHead, "+", CFGPrimitive.getPrimitive(24))));
        return assn;
    }

}
