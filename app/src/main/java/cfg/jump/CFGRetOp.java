package cfg.jump;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import cfg.BasicBlock;
import cfg.expr.data.CFGValue;
import cfg.expr.data.CFGVar;

public class CFGRetOp extends CFGJumpOp {
    private CFGValue val;

    public CFGRetOp(BasicBlock parent, CFGValue val) {
        super(parent);
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
        return "ret " + val;
    }

    @Override
    public void toSSA(BasicBlock parent, HashMap<String, CFGVar> varMap, HashMap<String, CFGVar> maxVer) {
        val = (CFGValue)val.toSSA(varMap);
    }

    @Override
    public void phiPlacementPass(HashSet<CFGVar> globals, BasicBlock current,
            HashMap<CFGVar, ArrayList<BasicBlock>> varBlocks, HashSet<CFGVar> varKill) {
        val.phiPlacementPass(globals, varKill);
    }
}
