package cfg.expr;

import cfg.expr.data.CFGVar;

public record CFGLoad(CFGVar base) implements CFGExpr {
    @Override
    public String toString() {
        return "load(" + base + ")";
    }
    
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof CFGLoad))
            return false;
        CFGLoad l = (CFGLoad)o;
        return l.base.equals(this.base);
    } 
}
