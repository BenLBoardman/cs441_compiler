package cfg.jump;

import cfg.BasicBlock;
import cfg.op.CFGOp;

public  abstract class CFGJumpOp extends CFGOp {
    protected BasicBlock parent;

    public CFGJumpOp(BasicBlock parent) {
        this.parent = parent;
    }
}
