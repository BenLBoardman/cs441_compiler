package parser.expression;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import cfg.BasicBlock;
import cfg.CtrlFlowGraph;
import cfg.expr.CFGCall;
import cfg.expr.CFGExpr;
import cfg.expr.CFGGet;
import cfg.expr.CFGLoad;
import cfg.expr.data.CFGPrimitive;
import cfg.expr.data.CFGValue;
import cfg.expr.data.CFGVar;
import cfg.op.CFGAssn;
import parser.ASTClass;
import parser.ASTMethod;

import java.util.Iterator;

import util.DataType;
import util.error.ErrorAccumulator;
import util.error.type.UndefinedClassError;

public class ASTMethodCall extends ASTExpression {
    private ASTExpression base;
    private String methodname;
    private List<ASTExpression> args;

    public ASTMethodCall(ASTExpression base, String methodname, List<ASTExpression> args) {
        this.base = base;
        this.methodname = methodname;
        this.args = args;
    }

    @Override
    public DataType getType(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) {
        DataType baseType = base.getType(types, symbols), passedType, expectedType;
        ASTClass classRef = types.get(baseType.typeName());
        ASTMethod method;
        Iterator<Entry<String,DataType>> argIterator;
        Entry<String, DataType> argEntry;
        if(classRef == null || classRef.type() == DataType.errType || classRef.type() == DataType.intType) {
            ErrorAccumulator.addError(new UndefinedClassError(0, baseType));
            return DataType.errType;
        }

        method = classRef.methods().get(methodname);
        if(method == null)
            throw new IllegalArgumentException("Error: Attempt to call nonexistent method "+methodname+" of class "+classRef.name()); //TODO new error system
        argIterator = method.args().entrySet().iterator();
        for(int i = 0; i < args.size(); i++) {
            if(!argIterator.hasNext())
                throw new IllegalArgumentException("Argument count mismatch calling method "+methodname); //TODO new error system
            argEntry = argIterator.next();
            passedType = args.get(i).getType(types, symbols);
            expectedType = argEntry.getValue();
            if(!passedType.equals(expectedType)) 
                throw new IllegalArgumentException("Argument "+i+" of method "+methodname+" should have type "+expectedType+", has actual type "+passedType); //TODO new error system
        }
        if(argIterator.hasNext())
            throw new IllegalArgumentException("Argument count mismatch calling method "+methodname); //TODO new error system
        return method.returnType();
    }

    @Override
    public CFGExpr toCFG(CFGVar assn, BasicBlock currBlock, boolean requireVal) {
        int methodId = CtrlFlowGraph.getMethodId(methodname);
        if (methodId == -1)
            throw new IllegalArgumentException("Attempt to call nonexistent method" + methodname);
        CFGVar obj = (CFGVar) base.toCFG(assn, currBlock, true);
        // load vtable, find method
        CFGVar vtbl = CFGVar.makeTmpVar(null);
        currBlock.addOp(new CFGAssn(vtbl, new CFGLoad(obj)));
        CFGVar methodAddr = CFGVar.makeTmpVar(null);
        currBlock.addOp(new CFGAssn(methodAddr, new CFGGet(vtbl, CFGPrimitive.getPrimitive(methodId)))); // get
                                                                                                           // vtable
                                                                                                           // id
        CFGVar callRslt = CFGVar.makeTmpVar(null);
        CFGValue[] args = new CFGValue[this.args.size()];
        for (int i = 0; i < args.length; i++) {
            args[i] = (CFGValue) this.args.get(i).toCFG(assn, currBlock, true);
        }

        currBlock.addOp(new CFGAssn(callRslt, new CFGCall(methodAddr, obj, args))); // figure out receiver
        return callRslt;
    }
}
