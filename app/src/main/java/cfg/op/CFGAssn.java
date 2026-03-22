package cfg.op;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import cfg.BasicBlock;
import cfg.expr.CFGExpr;
import cfg.expr.data.CFGVar;

public class CFGAssn extends CFGOp {
    private CFGVar var;
    private CFGExpr expr;

    @Override
    public String toString() {
        return var + " = " + expr;
    }

    public CFGVar var() {
        return var;
    }

    public void setVar(CFGVar var) {
        this.var = var;
    }

    public CFGExpr expr() {
        return expr;
    }

    public void setExpr(CFGExpr expr) {
        this.expr = expr;
    }

    public CFGAssn(CFGVar var, CFGExpr expr) {
        this.var = var;
        this.expr = expr;
    }

    @Override
    public void toSSA(BasicBlock parent, HashMap<String, CFGVar> varMap, HashMap<String, CFGVar> maxVer) {
        expr = expr.toSSA(varMap);
        // do whatever thing needs to be added for exprs
        CFGVar base = var;
        CFGVar storedVar = varMap.get(base.name());
        if (storedVar == null) // assignment to temporary value
            return;
        CFGVar newVar = new CFGVar(storedVar, storedVar.type());
        var = newVar;
        varMap.replace(storedVar.name(), newVar);
        maxVer.replace(newVar.name(), newVar);
        parent.addActive(newVar);
    }

    @Override
    public void phiPlacementPass(HashSet<CFGVar> globals, BasicBlock current,
            HashMap<CFGVar, ArrayList<BasicBlock>> varBlocks, HashSet<CFGVar> varKill) {
        expr.phiPlacementPass(globals, varKill);
        varKill.add(var);
        ArrayList<BasicBlock> blocksAssigned = varBlocks.get(var);
        if (var.name().equals(""))
            return;
        else if (blocksAssigned == null)
            varBlocks.put(var, new ArrayList<>(Arrays.asList(current)));
        else if (!blocksAssigned.contains(current))
            blocksAssigned.add(current);
    }
}
