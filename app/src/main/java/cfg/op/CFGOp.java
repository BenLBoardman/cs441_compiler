package cfg.op;

import java.util.HashMap;

import cfg.BasicBlock;
import cfg.CFGElement;
import cfg.expr.data.CFGVar;

public abstract class CFGOp implements CFGElement {
    public abstract void toSSA(BasicBlock parent, HashMap<String, CFGVar> varMap, HashMap<String, CFGVar> maxVer);
}