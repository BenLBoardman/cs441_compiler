package cfg.op;

import cfg.expr.data.CFGData;
import cfg.expr.data.CFGVar;

public non-sealed class CFGStore implements CFGOp {
    private CFGVar base;
    private CFGData index;

    public CFGVar base() {
        return base;
    }

    public void setBase(CFGVar base) {
        this.base = base;
    }

    public CFGData index() {
        return index;
    }

    public void setIndex(CFGData i) {
        this.index = i;
    }

    public CFGStore(CFGVar base, CFGData i) {
        this.base = base;
        this.index = i;
    }

    @Override
    public String toString() {
        return "store(" + base + ", " + index + ")";
    }
}
