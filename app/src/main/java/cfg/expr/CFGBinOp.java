package cfg.expr;

import cfg.expr.data.CFGValue;
import cfg.expr.data.CFGVar;

import java.util.HashMap;

import cfg.expr.data.CFGPrimitive;

public class CFGBinOp extends CFGExpr {
    private CFGValue lhs, rhs;
    private String op;

    public CFGBinOp(CFGValue lhs, String op, CFGValue rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.op = op;
    }

    public CFGValue lhs() {
        return lhs;
    }

    public CFGValue rhs() {
        return rhs;
    }

    public String op() {
        return op;
    }

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

    @Override
    public CFGExpr toSSA(HashMap<String, CFGVar> varMap) {
        lhs = (CFGValue)lhs.toSSA(varMap);
        rhs = (CFGValue)rhs.toSSA(varMap);
        return this;
    }
}
