package cfg.jump;

import java.util.HashMap;

import cfg.BasicBlock;
import cfg.expr.data.CFGVar;

public class CFGFail extends CFGJumpOp
{
    private CFGFailOpt fail;

    public CFGFail(BasicBlock parent, CFGFailOpt fail) {
        super(parent);
        this.fail = fail;
    }

    @Override public String toString() {return "fail "+fail.name(); }

    @Override
    public void toSSA(BasicBlock parent, HashMap<String, CFGVar> varMap, HashMap<String, CFGVar> maxVer) {
        return;
    }
}
