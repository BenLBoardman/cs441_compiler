package cfg.jump;

import cfg.BasicBlock;
import cfg.expr.data.CFGValue;

public class CFGCondOp extends CFGJumpOp
{
    private CFGValue cond;
    private BasicBlock yes;
    private BasicBlock no;

    public CFGCondOp(BasicBlock parent, CFGValue cond, BasicBlock yes, BasicBlock no) {
        super(parent);
        this.cond = cond;
        this.yes = yes;
        this.no = no;
    }

    public CFGValue cond() {
        return cond;
    }
    public BasicBlock yes() {
        return yes;
    }
    public BasicBlock no() {
        return no;
    }

    public void setCond(CFGValue cond) {
        this.cond = cond;
    }

    @Override
    public String toString() {
        return "if " + cond + " then " + yes.getIdentifier() + " else " + no.getIdentifier();
    }
}
