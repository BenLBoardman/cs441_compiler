package cfg.jump;

import cfg.BasicBlock;
import cfg.expr.data.CFGValue;

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
}
