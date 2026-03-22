package cfg.op;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import cfg.BasicBlock;
import cfg.CFGElement;
import cfg.expr.data.CFGVar;

public abstract class CFGOp implements CFGElement {
    public abstract void toSSA(BasicBlock parent, HashMap<String, CFGVar> varMap, HashMap<String, CFGVar> maxVer);

    public void phiPlacementPass(HashSet<CFGVar> globals, BasicBlock current, HashMap<CFGVar, ArrayList<BasicBlock>> varBlocks, HashSet<CFGVar> varKill) {
        return;
    }
}