package cfg.op;

import cfg.expr.data.CFGData;
import cfg.expr.data.CFGValue;
import cfg.expr.data.CFGVar;

public non-sealed class CFGSet implements CFGOp{
    private CFGVar addr;
    private CFGValue index;
    private CFGData val;

    public CFGVar addr() {
        return addr;
    }

    public void setAddr(CFGVar addr) {
        this.addr = addr;
    }

    public CFGValue index() {
        return index;
    }

    public void setIndex(CFGValue index) {
        this.index = index;
    }

    public CFGData val() {
        return val;
    }

    public void setVal(CFGData val) {
        this.val = val;
    }

    public CFGSet(CFGVar addr, CFGValue index, CFGData val) {
        this.addr = addr;
        this.index = index;
        this.val = val;
    }



    @Override
    public String toString() {
        return "setelt(" + addr + ", " + index + ", " + val + ")";
    }
}

