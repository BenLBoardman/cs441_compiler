package cfg.jump;

public record CFGFail(CFGFailOpt fail) implements CFGJumpOp { @Override public String toString() {return "fail "+fail.name(); } }

