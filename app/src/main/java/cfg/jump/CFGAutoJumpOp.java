package cfg.jump;

import java.util.HashMap;

import cfg.BasicBlock;
import cfg.expr.data.CFGVar;

public class CFGAutoJumpOp extends CFGJumpOp {
    private BasicBlock target;

    public CFGAutoJumpOp(BasicBlock parent, BasicBlock target) {
        super(parent);
        this.target = target;
    }

    public BasicBlock target() {
        return target;
    }
    
    @Override
    public String toString() {
        return "jump " + target.getIdentifier();
    }

    @Override
    public void toSSA(BasicBlock parent, HashMap<String, CFGVar> varMap, HashMap<String, CFGVar> maxVer) {
        return;
    }
}
