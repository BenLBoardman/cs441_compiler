package cfg.expr.data;

public sealed interface CFGValue extends CFGData
    permits CFGVar, CFGPrimitive {}
