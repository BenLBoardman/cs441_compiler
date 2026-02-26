package cfg;

import java.util.ArrayList;
import java.util.HashSet;

import java.util.Iterator;

import cfg.expr.*;
import cfg.jump.*;
import util.Output;
import cfg.expr.data.*;

public record CFGMethod(String name, CFGVar[] args, CFGVar[] locals, BasicBlock addr, ArrayList<BasicBlock> blocks, ArrayList<CFGVar> vars) implements CFGElement {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(name.equals("main"))
            sb.append("main:\n"+addr);
        else {
            sb.append(name + "(");
            for (int i = 0; i < args.length; i++) {
                CFGVar v = args[i];
                sb.append(v);
                if (i < args.length - 1)
                    sb.append(", ");
            }
            sb.append("):\n" + addr);
        }
        for (int i = 1; i < blocks.size(); i++) {
            sb.append("\n"+blocks.get(i).getIdentifier()+":\n").append(blocks.get(i));
        }
        return sb.toString();
    }

    public void addVar(CFGVar v) {
        vars.add(v);
    }

    public void condenseBlocks() {
        boolean changed = true;
        while(changed) {
            changed = false;
            for(BasicBlock b : blocks) {
                switch (b.getJmp()) {
                    case CFGAutoJumpOp a:
                        BasicBlock succ = a.target();
                        HashSet<BasicBlock> targetSuccs = succ.getSuccs();
                        if(succ.getPreds().size() == 1) { //b is only prececessor of succ
                            b.addOps(succ.getOps());
                            b.setJmp(succ.getJmp());
                            succ.setJmp(new CFGRetOp(CFGPrimitive.getPrimitive(0)));
                            b.removeSucc(succ);
                            for(BasicBlock s : new HashSet<BasicBlock>(targetSuccs)) {
                                s.replacePred(succ, b);
                            }
                            changed = true;
                        }
                        break;
                    case CFGCondOp c:
                        CFGExpr cond = c.cond();
                        if(cond instanceof CFGPrimitive) { //if will always evaluate to same value
                            changed = true;
                            long val = ((CFGPrimitive)cond).value();
                            BasicBlock target = val > 0 ? c.yes() : c.no(); //branch always taken
                            BasicBlock fakeBranch = val < 0 ? c.yes() : c.no(); //branch never taken
                            b.setJmp(new CFGAutoJumpOp(target));
                            b.removeSucc(fakeBranch);
                        }
                        break;
                    default: //return is unaffected
                        break;
                }
            }
            Output.debug(this.toString());
            HashSet<BasicBlock> deadBlocks = new HashSet<>(); 
           for(BasicBlock b : blocks) {
                if(b.getPreds().size() == 0 && b != blocks.get(0)) {
                    deadBlocks.add(b);
                    
                    Iterator<BasicBlock> succIter = b.getSuccs().iterator();
                    for(int i = b.getSuccs().size() - 1; i >= 0; i--)
                        b.removeSucc(succIter.next());
                }
            }
            blocks.removeAll(deadBlocks);
        }
    }
}
