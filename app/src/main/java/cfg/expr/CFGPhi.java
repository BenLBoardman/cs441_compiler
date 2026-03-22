package cfg.expr;

import java.util.ArrayList;
import java.util.HashSet;

import cfg.BasicBlock;
import cfg.expr.data.CFGValue;
import cfg.expr.data.CFGVar;

public class CFGPhi extends CFGExpr {
    private ArrayList<BasicBlock> blocks;
    private ArrayList<CFGValue> varVersions;

    public CFGPhi(ArrayList<BasicBlock> blocks, ArrayList<CFGValue> varVersions) {
        this.blocks = blocks;
        this.varVersions = varVersions;
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("phi(");
        for(int i = 0; i < blocks.size(); i++) {
            sb.append(blocks.get(i).getIdentifier()).append(", ");
            sb.append(varVersions.get(i)).append((i < blocks.size() - 1) ? ", " : "");
        }
        return sb.append(")").toString();
    }

    public ArrayList<BasicBlock> blocks() {
        return blocks;
    }


    public ArrayList<CFGValue> varVersions() {
        return varVersions;
    }

    @Override
    public void phiPlacementPass(HashSet<CFGVar> globals, HashSet<CFGVar> varKill) {
        //this should be an error state
        return;
    }
}
