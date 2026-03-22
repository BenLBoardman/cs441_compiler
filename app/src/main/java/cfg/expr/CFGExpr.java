package cfg.expr;

import java.util.HashMap;
import java.util.HashSet;

import cfg.CFGElement;
import cfg.expr.data.CFGVar;

public abstract class CFGExpr implements CFGElement {
    public boolean equals(Object o) {
        return super.equals(o);
    }

    public CFGExpr toSSA(HashMap<String, CFGVar> varMap) {
        return this;
    }

    public abstract void phiPlacementPass(HashSet<CFGVar> globals, HashSet<CFGVar> varKill);

    public boolean readAcrossMultiBlocks(HashSet<CFGVar> varKill) {
        return false;
    }
}
