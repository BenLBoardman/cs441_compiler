package parser.expression;

import java.util.HashMap;

import cfg.BasicBlock;
import cfg.expr.CFGBinOp;
import cfg.expr.CFGExpr;
import cfg.expr.data.CFGValue;
import cfg.expr.data.CFGVar;
import cfg.op.CFGAssn;
import parser.ASTClass;
import util.DataType;
import util.error.ErrorAccumulator;
import util.error.type.BinopMismatchError;

public class ASTBinop extends ASTExpression {
    private ASTExpression lhs, rhs;
    private String op;

    public ASTBinop(ASTExpression lhs, String op, ASTExpression rhs){
        this.lhs = lhs;
        this.op = op;
        this.rhs = rhs;
    }

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

    @Override
    public CFGExpr toCFG(CFGVar assn, BasicBlock currBlock, boolean requireVal) {
        CFGExpr lhs, rhs;
        lhs = this.lhs.toCFG(assn, currBlock, true);
        rhs = this.rhs.toCFG(assn, currBlock, true);

        CFGVar tmp;
        if (lhs instanceof CFGBinOp) {
            tmp = CFGVar.makeTmpVar(null);
            BasicBlock.currBlock.addOp(new CFGAssn(tmp, lhs));
            lhs = tmp;
        }
        if (rhs instanceof CFGBinOp) {
            tmp = CFGVar.makeTmpVar(null);
            BasicBlock.currBlock.addOp(new CFGAssn(tmp, rhs));
            rhs = tmp;
        }

        return new CFGBinOp((CFGValue) lhs, this.op, (CFGValue) rhs).evalBinOp(currBlock, requireVal);
    }

}
