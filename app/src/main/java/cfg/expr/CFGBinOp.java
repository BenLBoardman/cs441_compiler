package cfg.expr;

import cfg.expr.data.CFGValue;
import cfg.expr.data.CFGPrimitive;

public record CFGBinOp(CFGValue lhs, String op, CFGValue rhs) implements CFGExpr {
    @Override
    public String toString() {
        return lhs + " " + op + " " + rhs;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof CFGBinOp))
            return false;
        CFGBinOp b = (CFGBinOp)o;
        return this.lhs == b.lhs && this.op == b.op && this.rhs == b.rhs;
    }

    public CFGExpr evalBinOp() {
        if (lhs instanceof CFGPrimitive && rhs instanceof CFGPrimitive) { // optimize out double-constant binops
            CFGPrimitive lprim = (CFGPrimitive) lhs;
            CFGPrimitive rprim = (CFGPrimitive) rhs;
            long rslt;
            switch (op) {
                case "+":
                    rslt = lprim.value() + rprim.value();
                    break;
                case "-":
                    rslt = lprim.value() - rprim.value();
                    break;
                case "*":
                    rslt = lprim.value() * rprim.value();
                    break;
                case "/":
                    rslt = lprim.value() / rprim.value();
                    break;
                case ">":
                    rslt = lprim.value() > rprim.value() ? 1 : 0;
                    break;
                case "<":
                    rslt = lprim.value() < rprim.value() ? 1 : 0;
                    break;
                case "<<":
                    rslt = lprim.value() << rprim.value();
                    break;
                case ">>":
                    rslt = lprim.value() >> rprim.value();
                    break;
                case "<=":
                    rslt = lprim.value() <= rprim.value() ? 1 : 0;
                    break;
                case ">=":
                    rslt = lprim.value() >= rprim.value() ? 1 : 0;
                    break;
                case "==":
                    rslt = lprim.value() == rprim.value() ? 1 : 0;
                    break;
                case "!=":rslt = lprim.value() != rprim.value() ? 1 : 0;
                    break;
                case "&": rslt = lprim.value() & rprim.value(); break;
                default: // should be unreachable
                    rslt = 0;
            }
            return CFGPrimitive.getPrimitive(rslt);
        }
        return this;
    }
}
