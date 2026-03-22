package cfg.op;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import cfg.BasicBlock;
import cfg.expr.data.CFGValue;
import cfg.expr.data.CFGVar;

public class CFGPrint extends CFGOp {
    private CFGValue val;
    
    public CFGPrint(CFGValue val) {
        this.val = val;
    }

    public CFGValue val() {
        return val;
    }

    public void setVal(CFGValue val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return "print(" + val + ")";
    }

    @Override
    public void toSSA(BasicBlock parent, HashMap<String, CFGVar> varMap, HashMap<String, CFGVar> maxVer) {
        val = (CFGValue) val.toSSA(varMap);
    }

    @Override
    public void phiPlacementPass(HashSet<CFGVar> globals, BasicBlock current,
            HashMap<CFGVar, ArrayList<BasicBlock>> varBlocks, HashSet<CFGVar> varKill) {
        val.phiPlacementPass(globals, varKill);
    }
}

