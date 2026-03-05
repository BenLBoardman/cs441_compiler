package cfg.expr;

import java.util.HashMap;

import cfg.CFGElement;
import cfg.expr.data.CFGVar;

public abstract class CFGExpr implements CFGElement {
    public boolean equals(Object o) {
        return super.equals(o);
    }

    public CFGExpr toSSA(HashMap<String, CFGVar> varMap) {
        return this;
    }
}
