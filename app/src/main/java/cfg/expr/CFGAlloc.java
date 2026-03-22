package cfg.expr;

import java.util.HashSet;

import cfg.expr.data.CFGPrimitive;
import cfg.expr.data.CFGVar;

public class CFGAlloc extends CFGExpr {
    private final CFGPrimitive size;

    public CFGAlloc(CFGPrimitive size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "alloc(" + size + ")";
    }

    @Override
    public void phiPlacementPass(HashSet<CFGVar> globals, HashSet<CFGVar> varKill) {
        return;
    }

}
