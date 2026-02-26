package cfg.jump;

import cfg.CFGElement;

public sealed interface CFGJumpOp extends CFGElement
    permits CFGAutoJumpOp, CFGRetOp, CFGCondOp, CFGFail {
}
