package cfg.expr.data;

import cfg.expr.CFGExpr;

public sealed interface CFGData extends CFGExpr
    permits CFGValue, CFGArray {}