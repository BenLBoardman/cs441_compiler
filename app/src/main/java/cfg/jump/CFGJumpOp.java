package cfg.jump;

import cfg.BasicBlock;
import cfg.CFGElement;

public  abstract class CFGJumpOp implements CFGElement {
    protected BasicBlock parent;

    public CFGJumpOp(BasicBlock parent) {
        this.parent = parent;
    }
}
