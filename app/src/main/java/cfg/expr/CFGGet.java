package cfg.expr;

import cfg.expr.data.CFGVar;

import java.util.HashMap;

import cfg.expr.data.CFGValue;

public class CFGGet extends CFGExpr {
    private CFGVar arr;
    private CFGValue val;

    public CFGGet(CFGVar arr, CFGValue val) {
        this.arr = arr;
        this.val = val;
    }

    @Override
    public String toString() {
        return "getelt(" + arr + ", " + val + ")";
    }

    @Override public boolean equals(Object o) {
        if(!(o instanceof CFGGet))
            return false;
        CFGGet g = (CFGGet)o;
        return g.arr.equals(this.arr) && g.val.equals(this.val);
    }

    @Override
    public CFGExpr toSSA(HashMap<String, CFGVar> varMap) {
        arr = (CFGVar)arr.toSSA(varMap);
        val = (CFGValue)val.toSSA(varMap);
        return this;
    }

    public CFGVar arr() {
        return arr;
    }

    public CFGValue val() {
        return val;
    }
}
