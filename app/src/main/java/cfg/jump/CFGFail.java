package cfg.jump;

import cfg.BasicBlock;

public class CFGFail extends CFGJumpOp
{
    private CFGFailOpt fail;

    public CFGFail(BasicBlock parent, CFGFailOpt fail) {
        super(parent);
        this.fail = fail;
    }

    @Override public String toString() {return "fail "+fail.name(); }
}
