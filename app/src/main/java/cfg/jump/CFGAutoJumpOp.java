package cfg.jump;

import cfg.BasicBlock;

public class CFGAutoJumpOp extends CFGJumpOp {
    private BasicBlock target;

    public CFGAutoJumpOp(BasicBlock parent, BasicBlock target) {
        super(parent);
        this.target = target;
    }

    public BasicBlock target() {
        return target;
    }
    
    @Override
    public String toString() {
        return "jump " + target.getIdentifier();
    }
}
