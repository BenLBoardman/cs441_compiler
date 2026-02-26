package cfg.op;

import cfg.CFGElement;

public sealed interface CFGOp extends CFGElement
    permits CFGAssn, CFGPrint, CFGSet, CFGStore {
}