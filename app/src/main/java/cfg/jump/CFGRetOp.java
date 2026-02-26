package cfg.jump;

import cfg.expr.data.CFGValue;

public non-sealed class CFGRetOp implements CFGJumpOp {
    private CFGValue val;

    public CFGRetOp(CFGValue val) {
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
