package cfg.expr;

import cfg.expr.data.CFGPrimitive;

public record CFGAlloc(CFGPrimitive size) implements CFGExpr {
    @Override
    public String toString() {
        return "alloc(" + size + ")";
    }
}
