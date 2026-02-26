package cfg.expr;

import cfg.expr.data.CFGValue;
import cfg.expr.data.CFGVar;

public record CFGCall(CFGVar addr, CFGVar receiver, CFGValue[] args) implements CFGExpr {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("call(");
        sb.append(addr + ", " + receiver);
        for(CFGValue a : args)
            sb.append(", ").append(a);
        return sb.append(')').toString();
    }
} 

