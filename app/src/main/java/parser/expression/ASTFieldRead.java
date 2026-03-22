package parser.expression;

import java.util.HashMap;

import cfg.BasicBlock;
import cfg.CFGClass;
import cfg.CtrlFlowGraph;
import cfg.expr.CFGExpr;
import cfg.expr.CFGGet;
import cfg.expr.data.CFGPrimitive;
import cfg.expr.data.CFGVar;
import cfg.op.CFGAssn;
import parser.ASTClass;
import util.DataType;
import util.error.ErrorAccumulator;
import util.error.type.UndefinedClassError;
import util.error.NoSuchFieldError;

public class ASTFieldRead extends ASTExpression {
    private ASTExpression base;
    private String fieldname;

    public ASTFieldRead(ASTExpression base, String fieldname) {
        this.base = base;
        this.fieldname = fieldname;
    }

    @Override
    public DataType getType(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) {
        DataType baseType = base.getType(types, symbols);
        ASTClass classRef = types.get(baseType.typeName());
        DataType fieldType;
        if(classRef == null || classRef.type() == DataType.errType || classRef.type() == DataType.intType) {
            ErrorAccumulator.addError(new UndefinedClassError(0, baseType));
            return DataType.errType;
        }
        fieldType = classRef.fields().get(fieldname);
        if(fieldType == null)
            throw new IllegalArgumentException("Error: Attempt to read nonexistent field "+fieldname+" of class "+classRef.name());
        return fieldType;
    }

    @Override
    public CFGExpr toCFG(CFGVar assn, BasicBlock currBlock, boolean requireVal) {
        CFGVar vtbl = (CFGVar) base.toCFG(assn, currBlock, true), field;
        CFGClass cl = CtrlFlowGraph.findClass(vtbl.type().typeName());
        int fieldId = cl.getFieldId(fieldname);
        if (fieldId == -1) {
            ErrorAccumulator.addError(new NoSuchFieldError(0, cl.name(), fieldname));
            return null;
        }
        field = CFGVar.makeTmpVar(null);
        currBlock.addOp(new CFGAssn(field, new CFGGet(vtbl, CFGPrimitive.getPrimitive(fieldId + 1))));
        return field;
    }
}
