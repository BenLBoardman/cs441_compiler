package cfg.expr;

import java.util.HashMap;

import cfg.expr.data.CFGVar;

public class CFGLoad extends CFGExpr {
    private CFGVar base;

    public CFGLoad(CFGVar base) {
        this.base = base;
    }

    @Override
    public String toString() {
        return "load(" + base + ")";
    }

    public CFGVar base() {
        return base;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof CFGLoad))
            return false;
        CFGLoad l = (CFGLoad)o;
        return l.base.equals(this.base);
    } 

    @Override
    public CFGExpr toSSA(HashMap<String, CFGVar> varMap) {
        base = (CFGVar)base.toSSA(varMap);
        return this;
    }
}
