package cfg.jump;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import cfg.BasicBlock;
import cfg.expr.data.CFGValue;
import cfg.expr.data.CFGVar;

public class CFGCondOp extends CFGJumpOp
{
    private CFGValue cond;
    private BasicBlock yes;
    private BasicBlock no;

    public CFGCondOp(BasicBlock parent, CFGValue cond, BasicBlock yes, BasicBlock no) {
        super(parent);
        this.cond = cond;
        this.yes = yes;
        this.no = no;
    }

    public CFGValue cond() {
        return cond;
    }
    public BasicBlock yes() {
        return yes;
    }
    public BasicBlock no() {
        return no;
    }

    public void setCond(CFGValue cond) {
        this.cond = cond;
    }

    @Override
    public String toString() {
        return "if " + cond + " then " + yes.getIdentifier() + " else " + no.getIdentifier();
    }

    @Override
    public void toSSA(BasicBlock parent, HashMap<String, CFGVar> varMap, HashMap<String, CFGVar> maxVer) {
        cond = (CFGValue)cond.toSSA(varMap);
    }

    @Override
    public void phiPlacementPass(HashSet<CFGVar> globals, BasicBlock current,
            HashMap<CFGVar, ArrayList<BasicBlock>> varBlocks, HashSet<CFGVar> varKill) {
        cond.phiPlacementPass(globals, varKill);
    }
}
