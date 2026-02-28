package cfg.expr;

import cfg.expr.data.CFGPrimitive;

public class CFGAlloc extends CFGExpr {
    private final CFGPrimitive size;

    public CFGAlloc(CFGPrimitive size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "alloc(" + size + ")";
    }

}
