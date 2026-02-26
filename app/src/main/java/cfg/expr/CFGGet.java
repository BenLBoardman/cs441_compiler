package cfg.expr;

import cfg.expr.data.CFGVar;
import cfg.expr.data.CFGValue;

public record CFGGet(CFGVar arr, CFGValue val) implements CFGExpr {
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
}
