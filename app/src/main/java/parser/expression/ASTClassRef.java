package parser.expression;

import java.util.HashMap;

import cfg.BasicBlock;
import cfg.CFGClass;
import cfg.CtrlFlowGraph;
import cfg.expr.CFGAlloc;
import cfg.expr.CFGBinOp;
import cfg.expr.CFGExpr;
import cfg.expr.data.CFGPrimitive;
import cfg.expr.data.CFGVar;
import cfg.op.CFGAssn;
import cfg.op.CFGSet;
import cfg.op.CFGStore;
import parser.ASTClass;
import util.DataType;

public class ASTClassRef extends ASTExpression {
    private String classname;

    public ASTClassRef(String classname) {
        this.classname = classname;
    }

    @Override
    public DataType getType(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) {
        return types.get(classname).type();
    }

    @Override
    public CFGExpr toCFG(CFGVar assn, BasicBlock currBlock, boolean requireVal) {
        CFGClass classData = CtrlFlowGraph.findClass(classname);
        CFGVar vtPtr = assn;
        CFGVar cPtr;
        if (classData == null)
            throw new IllegalArgumentException("Class " + classname + " is undefined");
        cPtr = CFGVar.makeTmpVar(null);

        currBlock.addOp(new CFGAssn(cPtr, new CFGAlloc(
                CFGPrimitive.getPrimitive(classData.numFields() + 4)))); // fields plus three GC slots plus vtable ptr
        if (vtPtr == null) {
            vtPtr = CFGVar.makeTmpVar(null);
            currBlock.addActive(vtPtr);
        }
        currBlock.addOp(new CFGSet(cPtr, CFGPrimitive.getPrimitive(2), classData.getBitMap())); // set bitmap
        currBlock.addOp(new CFGAssn(vtPtr, new CFGBinOp(cPtr, "+", CFGPrimitive.getPrimitive(24)))); // assign vtable
                                                                                                     // pointer
        currBlock.addOp(new CFGStore(vtPtr, classData.vtable()));
        return vtPtr;
    }}
