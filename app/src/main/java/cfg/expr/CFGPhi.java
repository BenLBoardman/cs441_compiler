package cfg.expr;

import java.util.ArrayList;

import cfg.BasicBlock;
import cfg.expr.data.CFGValue;

public record CFGPhi(ArrayList<BasicBlock> blocks, ArrayList<CFGValue> varVersions) implements CFGExpr {
    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("phi(");
        for(int i = 0; i < blocks.size(); i++) {
            sb.append(blocks.get(i).getIdentifier()).append(", ");
            sb.append(varVersions.get(i)).append((i < blocks.size() - 1) ? ", " : "");
        }
        return sb.append(")").toString();
    }
}
