package cfg.expr;

import java.util.HashMap;

import cfg.expr.data.CFGValue;
import cfg.expr.data.CFGVar;

public class CFGCall extends CFGExpr {
    private CFGVar addr, receiver;
    private CFGValue[] args;

    public CFGCall(CFGVar addr, CFGVar receiver, CFGValue[] args) {
        this.addr = addr;
        this.receiver = receiver;
        this.args = args;
    }

    @Override
    public CFGExpr toSSA(HashMap<String, CFGVar> varMap) {
        addr = (CFGVar) addr.toSSA(varMap);
        receiver = (CFGVar) receiver.toSSA(varMap);
        for (int i = 0; i < args.length; i++) {
            args[i] = (CFGValue) args[i].toSSA(varMap);        
        }
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("call(");
        sb.append(addr + ", " + receiver);
        for (CFGValue a : args)
            sb.append(", ").append(a);
        return sb.append(')').toString();
    }

    public CFGVar addr() {
        return addr;
    }

    public CFGVar receiver() {
        return receiver;
    }

    public CFGValue[] args() {
        return args;
    }
}
