package cfg.jump;

import cfg.BasicBlock;

public record CFGAutoJumpOp(BasicBlock target) implements CFGJumpOp { @Override public String toString() { return "jump " + target.getIdentifier(); } }

