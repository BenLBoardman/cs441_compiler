package cfg;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import cfg.expr.*;
import cfg.jump.*;
import cfg.expr.data.*;
import cfg.op.*;
import parser.expression.*;
import parser.statement.*;
import util.DataType;
import util.Output;

public //identifier: name of the basic block
//actives: list of most recent variable versions active in the block
//ops: non-jumping operations, in order
//jump: jump, return, or conditional that ends the block
class BasicBlock {
    public static int blockId = 0;
    public static int ptrFails = 0;
    public static int numberFails = 0;
    public static int fieldFails = 0;
    public static int methodFails = 0;
    private static BasicBlock currBlock;

    private String identifier;
    private HashSet<CFGVar> actives;
    private ArrayList<CFGOp> ops;
    private ArrayList <CFGAssn> phis; //phis stored in their own list for simplicity
    
    private CFGJumpOp jmp;
    private HashSet<BasicBlock> preds;
    private ArrayList<BasicBlock> succs;
    
    
    public HashSet<BasicBlock> dominators; //blocks that dominate this block
    public HashSet<BasicBlock> inverseDominators; //blocks this block dominates
    public BasicBlock immediateDominator; //the "nearest" dominator in the CFG
    public HashSet<BasicBlock> dominanceFrontier; //blocks that *almost* dominate this block

    private boolean inSSA; //boolean determining if block is already in SSA - used to avoid infinite loops


    public ArrayList<CFGOp> getOps() {
        return ops;
    }
    
    //replace predecessor "a" with "b"
    public void replacePred(BasicBlock a, BasicBlock b) {
       preds.remove(a);
       preds.add(b);
       a.succs.remove(this);
       b.succs.add(this);
    }

    public void setJmp(CFGJumpOp jmp) {
        this.jmp = jmp;
    }

    public void addOps(ArrayList<CFGOp> ops) {
        this.ops.addAll(ops);
    }

    public void removeSucc(BasicBlock s) {
        succs.remove(s);
        s.preds.remove(this);
    }

    public CFGJumpOp getJmp() {
       return jmp;
    }

    public void doLocalValueNumbering() {
        ArrayList<CFGExpr> vn = new ArrayList<>();
        ArrayList<CFGVar> names = new ArrayList<>(); //list of already-defined variables
        ArrayList<CFGOp> deadOps = new ArrayList<>();
        int index;
        boolean changed = true;
        while (changed) {
            changed = false;
            vn.clear();
            for (CFGOp o : ops) {
                switch (o) {
                    case CFGAssn a:
                        CFGVar precalc, v = a.var();
                        CFGExpr expr = a.expr();
                        if (expr instanceof CFGBinOp) //evaluate binary op (if both primitives) - basically poor-man's constant propagation
                            expr = ((CFGBinOp) expr).evalBinOp();
                        a.setExpr(expr);
                        if (expr instanceof CFGBinOp) {
                            index = vn.indexOf(expr);
                            if (index != -1) {
                                changed = true;
                                precalc = names.get(index);
                                deadOps.add(a);
                                replaceGlobalUsages(new ArrayList<>(), v, precalc);
                                // traverse through block & replace exprs containing v with precalc
                            } else {
                                names.add(v);
                                vn.add(expr);
                            }
                            // alloc is ignored since classes need to be instantiated separately
                            // call & get are ignored since side effects exist
                            // var & primitive are handled separately
                            // phi is ignored since phis shouldn't be changed by VN (phis also won't be in
                            // Ops at this point)
                        } else if (expr instanceof CFGVar || expr instanceof CFGPrimitive) {
                            deadOps.add(a);
                            replaceGlobalUsages(new ArrayList<>(), v, (CFGValue) expr);
                        }
                        break;
                    default: // non-assignment operations are ignored
                        break;
                }
            }
            for (CFGOp o : deadOps) {
                ops.remove(o);
            }
        }
    }

    //replace ALL usages of oldVar in the method with newVar
    public void replaceGlobalUsages(ArrayList<BasicBlock> replaced, CFGVar oldVar, CFGValue newVar) {
        if(replaced.contains(this))
            return;
        replaced.add(this);
        this.replaceUsages(oldVar, newVar); //replace all usages in this block
        for(BasicBlock s : this.succs) {
            s.replaceGlobalUsages(replaced, oldVar, newVar); //replace usages in successors
        }
    }

    //replace usages of the CFGVar old with new (in expressions)
    public void replaceUsages(CFGVar oldVar, CFGValue newVar) {
        for(CFGAssn p : phis) {
             p.setExpr(replaceUsagesExpr(p.expr(), oldVar, newVar));
        }
        for(CFGOp o : ops) {
            switch (o) {
                //need cases for all op types calling replaceUsagesExpr on their exprs
                case CFGAssn a:
                    a.setExpr(replaceUsagesExpr(a.expr(), oldVar, newVar));
                    break;
                case CFGStore s:
                    s.setBase((CFGVar)replaceUsagesExpr(s.base(), oldVar, newVar));
                    s.setIndex((CFGData)replaceUsagesExpr(s.index(), oldVar, newVar));
                    break;
                case CFGSet s:
                    s.setAddr((CFGVar)replaceUsagesExpr(s.addr(), oldVar, newVar));
                    s.setIndex((CFGValue)replaceUsagesExpr(s.index(), oldVar, newVar));
                    s.setVal((CFGData)replaceUsagesExpr(s.val(), oldVar, newVar));
                    break;
                case CFGPrint p:
                    p.setVal((CFGValue)replaceUsagesExpr(p.val(), oldVar, newVar));
                    break;
                default:
                    break;
            }
        }
        switch (jmp) {
            case CFGRetOp r:
                r.setVal((CFGValue)replaceUsagesExpr(r.val(), oldVar, newVar));
                break;
            case CFGCondOp c:
                c.setCond((CFGValue)replaceUsagesExpr(c.cond(), oldVar, newVar));
                break;
            default:
                break;
        }
    }
    
    //replace usages of oldVar with newVar in the expression e
    public CFGExpr replaceUsagesExpr(CFGExpr e, CFGVar oldVar, CFGValue newVar) {
        switch(e) {
            case CFGVar v:
                return v.equals(oldVar) ? newVar : v;
            case CFGBinOp b:
                return new CFGBinOp(oldVar.equals(b.lhs()) ? newVar : b.lhs(), b.op(), oldVar.equals(b.rhs()) ? newVar : b.rhs());
            case CFGGet g:
                return new CFGGet(oldVar.equals(g.arr()) ? (CFGVar)newVar : g.arr(), oldVar.equals(g.val()) ? newVar : g.val());
            case CFGLoad l:
                return new CFGLoad(oldVar.equals(l.base()) ? (CFGVar)newVar : l.base());
            case CFGCall c:
                CFGValue[] newArgs = new CFGValue[c.args().length];
                for(int i = 0; i < newArgs.length; i++) {
                    CFGValue oldArg = c.args()[i];
                    newArgs[i] = oldVar.equals(oldArg) ? newVar : oldArg;
                }
                return new CFGCall(oldVar.equals(c.addr()) ? (CFGVar)newVar : c.addr(), 
                    oldVar.equals(c.receiver()) ? (CFGVar)newVar : c.receiver(), newArgs);
            case CFGPhi p:
                ArrayList<CFGValue> vars = p.varVersions();
                for(int i = 0; i < vars.size(); i++) {
                    CFGValue var = vars.get(i);
                    vars.set(i, oldVar.equals(var) ? newVar : var);
                }
                return p;
            default: //CFGPrimitive, 
                return e;

        }
    }

    public void addPhi(CFGVar v) {
        ArrayList<CFGValue> vars = new ArrayList<>();
        ArrayList<BasicBlock> blocks = new ArrayList<>();
        for(BasicBlock p : preds) {
            vars.add(v);
            blocks.add(p);
        }
        CFGAssn newPhi = new CFGAssn(v, new CFGPhi(blocks, vars));
        phis.add(newPhi);
    }
    
    public boolean hasPhi(CFGVar v) {
        for(CFGAssn p : phis) {
            if(p.var().equals(v))
                return true;
        }
        return false;
    }
    
    public ArrayList<CFGAssn> getPhis() {
        return phis;
    }

    public BasicBlock(ArrayList<BasicBlock> blocksInMethod, String blockBaseName, ArrayList <ASTStatement> stmts, int startIndex, HashSet<CFGVar> actives, 
        HashSet<BasicBlock> preds, CFGVar[] locals, BasicBlock jmpBack) {
            this(blocksInMethod, preds, actives);
            this.setupBlock(blocksInMethod, blockBaseName, stmts, startIndex, locals, jmpBack);
        }
    
    public ArrayList<BasicBlock> getSuccs() {
        return succs;
    }

    public BasicBlock(ArrayList<BasicBlock> blocksInMethod) { //placeholder constructor to just initialize arraylists
        inSSA = false;
        currBlock = this;
        blocksInMethod.add(this);
        CtrlFlowGraph.basicBlocks.add(this);
        preds = new HashSet<>();
        succs = new ArrayList<>();
        actives = new HashSet<>();
        dominators = new HashSet<>();
	    inverseDominators = new HashSet<>();
        dominanceFrontier = new HashSet<>();
        ops = new ArrayList<>();
        phis = new ArrayList<>();
        jmp = null;
        return;
    }

    // create empty basic block with predecessor & active var setup
    // used where basic blocks are built manually (while, method call, field r/w)
    public BasicBlock(ArrayList<BasicBlock> blocksInMethod, HashSet<BasicBlock> preds, HashSet<CFGVar> actives) {
        this(blocksInMethod);
        setPredsActives(preds, actives);
    }

    //create a fail block
    public BasicBlock(ArrayList<BasicBlock> blocksInMethod, CFGFailOpt failType, HashSet<BasicBlock> preds) {
        this(blocksInMethod);
        this.preds = new HashSet<>(preds);
        for(BasicBlock p : this.preds) {
            p.succs.add(this);
        }
        jmp = new CFGFail(this, failType);
        identifier = failType.toString();
        switch (failType) {
            case CFGFailOpt.NotANumber:
                identifier = identifier+numberFails;
                numberFails++;
                break;
            case CFGFailOpt.NotAPointer:
                identifier = identifier+ptrFails;
                ptrFails++;
                break;
            case CFGFailOpt.NoSuchField:
                    identifier = identifier+fieldFails;
                    fieldFails++;
                    break;
            case CFGFailOpt.NoSuchMethod:
                identifier = identifier+methodFails;
                methodFails++;
                break;
        }
    }

    //do majority of work to actually set up block - despite being non-static, mostly operates on the static field currBlock
    private void setupBlock(ArrayList<BasicBlock> blocksInMethod, String blockBaseName, ArrayList <ASTStatement> stmts, int startIndex, 
        CFGVar[] locals, BasicBlock jmpBack) {
        if(identifier == null || identifier.equals(""))
            setIdentifier(blockBaseName);
        HashSet<BasicBlock> localPreds = new HashSet<>();
        //make phis
        BasicBlock afterIf=null, ifBlk, branchEntryBlock;
        CFGValue cond;
        for(int i = startIndex; i < stmts.size(); i++) {
            ASTStatement s = stmts.get(i);
            switch (s) {
                case ASTAssignStmt a:
                    CFGVar assignment = null;
                    String name = a.var().name();
                    CFGVar base = getActive(name);
                    if(base != null) {
                        if(base.isThis())
                            throw new IllegalArgumentException("Error: illegal write to \"this\"");
                        assignment = base; //variable has already been initialized, we are reassigning it
                    } 
                    for(CFGVar v : locals) {
                        if(v.name().equals(name)) {
                            assignment = v; //variable is a local that has not been initalized yet, we need to initialize it
                            currBlock.actives.add(assignment);
                            break;
                        }
                    }
                    CFGExpr operand = exprToCFG(assignment, blocksInMethod, blockBaseName, a.rhs(), locals, false);
                    //CFGExpr tagged = operand;
                    //if(operand instanceof CFGBinOp) {
                    //    tmp = new CFGVar(tmp);
                    //    tmp.setShouldTag(true);
                    //    tagged = tmp;
                    //    currBlock.addOp(new CFGAssn((CFGVar)tagged, operand));    
                    //}
                    if(assignment == null)
                        throw new IllegalArgumentException("Post-Parse error: Cannot initialize variable "+name+" as it was neither passed as an argument nor declared as a local.");
                    if(!(operand instanceof CFGBinOp) && !CtrlFlowGraph.isTyped())
                        operand = tagInts(assignment, operand);
                    if(assignment != operand)
                        currBlock.addOp(new CFGAssn(assignment, operand));
                    break;
                case ASTIfElseStmt ie:
                    cond = (CFGValue)exprToCFG(null, blocksInMethod, blockBaseName, ie.cond(), locals, true);
                    if(cond instanceof CFGVar && !((CFGVar)cond).isTmp())
                        cond = checkUntagInt(null, (CFGVar)cond, blocksInMethod, blockBaseName, localPreds);
                    localPreds.add(currBlock);
                    branchEntryBlock = currBlock;
                    ifBlk = new BasicBlock(blocksInMethod);
                    if(i < stmts.size()-1) {
                        afterIf = new BasicBlock(blocksInMethod);
                        blocksInMethod.remove(blocksInMethod.indexOf(afterIf));
                    }
                    ifBlk.setPredsActives(localPreds, actives);
                    
                    currBlock = ifBlk;
                    ifBlk.setupBlock(blocksInMethod, blockBaseName, ie.body(), 0, locals, afterIf); 
                    BasicBlock endIf = currBlock;
                    BasicBlock elseBlk = new BasicBlock(blocksInMethod);
                    elseBlk.setPredsActives(localPreds, actives);
                    currBlock = elseBlk;
                    elseBlk.setupBlock(blocksInMethod, blockBaseName, ie.elseBody(), 0, locals, afterIf);
                    localPreds.remove(branchEntryBlock);
                    localPreds.add(currBlock);
                    localPreds.add(endIf);
                    if(afterIf != null){
                        blocksInMethod.add(afterIf);
                        currBlock = afterIf;
                        afterIf.setPredsActives(localPreds, actives);
                        afterIf.setupBlock(blocksInMethod, blockBaseName, stmts, i + 1, locals, jmpBack);
                    }
                    branchEntryBlock.jmp = new CFGCondOp(branchEntryBlock, cond, ifBlk, elseBlk);
                    localPreds.clear();
                    return;
                case ASTIfOnlyStmt io:
                    cond = (CFGValue)exprToCFG(null, blocksInMethod, blockBaseName, io.cond(), locals, true);
                    if(cond instanceof CFGVar && !((CFGVar)cond).isTmp())
                        cond = checkUntagInt(null, (CFGVar)cond, blocksInMethod, blockBaseName, localPreds);
                    localPreds.add(currBlock);
                    branchEntryBlock = currBlock;
                    ifBlk = new BasicBlock(blocksInMethod);
                    ifBlk.setPredsActives(localPreds, actives);
                    currBlock = ifBlk;
                    ifBlk.setupBlock(blocksInMethod, blockBaseName, io.body(), 0, locals, afterIf);
                    afterIf = new BasicBlock(blocksInMethod);
                    afterIf.setPredsActives(localPreds, actives);
                    afterIf.setupBlock(blocksInMethod, blockBaseName, stmts, i+1, locals, jmpBack);
                    branchEntryBlock.jmp = new CFGCondOp(branchEntryBlock, cond, ifBlk, afterIf);
                    localPreds.clear();
                    return;
                case ASTWhileStmt w:
                    branchEntryBlock = currBlock;
                    localPreds.add(branchEntryBlock);
                    BasicBlock loopheadStart = new BasicBlock(blocksInMethod);
                    loopheadStart.setPredsActives(localPreds, actives);
                    localPreds.remove(branchEntryBlock);
                    loopheadStart.setIdentifier(blockBaseName);
                    cond = (CFGValue)exprToCFG(null, blocksInMethod, blockBaseName, w.cond(), locals, true);
                    if(cond instanceof CFGVar && !((CFGVar)cond).isTmp())
                        cond = checkUntagInt(null, (CFGVar)cond, blocksInMethod, blockBaseName, localPreds);
                    BasicBlock loopheadEnd = currBlock;
                    localPreds.add(loopheadEnd);
                    loopheadEnd.addActives(actives);
                    BasicBlock body = new BasicBlock(blocksInMethod, blockBaseName, w.body(), 0, actives, localPreds, locals, loopheadStart);
                    loopheadEnd.addPred(body);
                    BasicBlock after = new BasicBlock(blocksInMethod, blockBaseName, stmts , i + 1, actives, localPreds, locals, jmpBack);
                    localPreds.remove(loopheadEnd);
                    loopheadEnd.addJump(new CFGCondOp(loopheadEnd, cond, body, after)); //add jump at end
                    branchEntryBlock.jmp = new CFGAutoJumpOp(branchEntryBlock, loopheadStart);  
                    return;
                case ASTPrintStmt p:
                    CFGValue prt = (CFGValue)exprToCFG(null, blocksInMethod, blockBaseName, p.str(), locals, true);
                    if(prt instanceof CFGVar) { //need to untag int & make sure we're not dereferencing a ptr
                        prt = checkUntagInt(null, (CFGVar)prt, blocksInMethod, blockBaseName, localPreds);                        
                    }
                    currBlock.addOp(new CFGPrint(prt));
                    break;
                case ASTFieldWriteStmt f: // can break if writing ptr to field
                    CFGValue objToStore = (CFGValue)currBlock.exprToCFG(null, blocksInMethod, blockBaseName, f.rhs(), locals, true); //evaluate rhs first
                    //can safely cast obj since it is known to be a var identifier by tokenizer
                    CFGVar obj = (CFGVar)exprToCFG(null, blocksInMethod, blockBaseName, f.base(), locals, true);
                    int fieldId = CtrlFlowGraph.getFieldId(f.fieldname()); //get index of field in fields arr
                    if(fieldId == -1)
                        throw new IllegalArgumentException("Attempt to modify never-declared field "+f.fieldname());
                    genPtrTagChk(obj, blocksInMethod, blockBaseName, localPreds); //gen tag check for obj
                    BasicBlock getField = currBlock;
                    CFGVar field;
                    if(!CtrlFlowGraph.isTyped()) {
                        CFGVar fieldsAddr = CFGVar.makeTmpVar(null);
                        CFGVar fields = CFGVar.makeTmpVar(null);   
                        getField.addOp(new CFGAssn(fieldsAddr, new CFGBinOp(obj, "+", CFGPrimitive.getPrimitive(8))));
                        getField.addOp(new CFGAssn(fields, new CFGLoad(fieldsAddr)));
                        field = CFGVar.makeTmpVar(null);
                        getField.addOp(new CFGAssn(field, new CFGGet(fields, CFGPrimitive.getPrimitive(fieldId))));
                        localPreds.remove(currBlock);
                        localPreds.add(getField);
                        BasicBlock fieldFail = new BasicBlock(blocksInMethod, CFGFailOpt.NoSuchField, localPreds);
                        BasicBlock storeVar = new BasicBlock(blocksInMethod, localPreds, actives);
                        getField.jmp = new CFGCondOp(getField, field, storeVar, fieldFail); // test for no such field
                        if (objToStore instanceof CFGPrimitive)
                            objToStore = (CFGValue) tagInt(null, objToStore);
                        currBlock.addOp(new CFGSet(obj, field, objToStore));
                        currBlock.setupBlock(blocksInMethod, blockBaseName, stmts, i + 1, locals, jmpBack);
                        return; // rest of method is processed by setupBlock
                    } else {
                        CFGVar offset = CFGVar.makeTmpVar(null);
                        CFGClass cl = CtrlFlowGraph.findClass(obj.type().typeName());
                        fieldId = cl.getFieldId(f.fieldname());
                        getField.addOp(new CFGAssn(offset, new CFGBinOp(obj, "+", CFGPrimitive.getPrimitive(8*fieldId))));
                        getField.addOp(new CFGStore(offset, objToStore));
                    }
                    break;
                case ASTReturnStmt r:
                    CFGValue valToReturn = (CFGValue)exprToCFG(null, blocksInMethod, blockBaseName, r.output(), locals, true);
                    if(valToReturn instanceof CFGPrimitive)
                        valToReturn = (CFGValue)tagInt(null, valToReturn);
                    currBlock.jmp = new CFGRetOp(currBlock, valToReturn);
                    break;
                case ASTVoidStmt v:
                    CFGValue voidRslt = (CFGValue)exprToCFG(null, blocksInMethod, blockBaseName, v.rhs(), locals, true);
                    currBlock.addOp(new CFGAssn(CFGVar.makeTmpVar(null), voidRslt));
                    break;
                    default:
                    break;
            }
            if(jmp != null && !(i == stmts.size()-1)) { //basic block has been finished in a submethod
                currBlock.setupBlock(blocksInMethod, blockBaseName, stmts, i + 1, locals, jmpBack);
                return;
            }
        }
        if (currBlock.jmp == null) {
            if (jmpBack == null)
                currBlock.jmp = new CFGRetOp(currBlock, CFGPrimitive.getPrimitive(0));
            else {
                jmpBack.addPred(currBlock);
                currBlock.jmp = new CFGAutoJumpOp(currBlock, jmpBack);
            }
        }
        return;
    }

    public void toSSA(HashMap<String, CFGVar> varMap, HashMap<String, CFGVar> maxVer) {
        if(inSSA)
            return;
        inSSA = true;
        for(CFGAssn phi : phis) {
            CFGVar oldVer = maxVer.get(phi.var().name());
            CFGVar newVer = new CFGVar(oldVer, oldVer.type());
            phi.setVar(newVer);
            varMap.replace(oldVer.name(), newVer);
            maxVer.replace(oldVer.name(), newVer);
        }
        for(CFGOp o : ops) {
            opToSSA(o, varMap, maxVer);
        }
        jumpToSSA(varMap);
        for(BasicBlock succ : succs) { //put all succs of this block that it also dominates into SSA
            Output.debug("Updating phis for successor: "+succ.identifier);
            HashMap<String, CFGVar> outVars = new HashMap<>(varMap);
            for (CFGAssn a : succ.phis) {
                CFGVar phiVar = a.var();
                CFGVar updatedVar = outVars.get(phiVar.name());
                CFGPhi phiOp = (CFGPhi)a.expr();
                for(int i = 0; i < phiOp.blocks().size(); i++) {
                    if(phiOp.blocks().get(i) == this) {
                        phiOp.varVersions().set(i, updatedVar);
                        if(updatedVar.version() == -1) {
                            System.out.println("Error: Variable "+updatedVar.name()+" may be used before being initialized.");
                            System.exit(1);
                        }
                    }
                    
                }
            }
            if(inverseDominators.contains(succ)) {
                Output.debug(identifier + " dominates " + succ.identifier);
                succ.toSSA(outVars, maxVer);
            }
        }
    }

    void opToSSA (CFGOp o, HashMap<String, CFGVar> varMap, HashMap<String, CFGVar> maxVer) {
        switch (o) {
            case CFGAssn a:
                a.setExpr(a.expr().toSSA(varMap));
                // do whatever thing needs to be added for exprs
                CFGVar base = a.var();
                CFGVar storedVar = varMap.get(base.name());
                if (storedVar == null) // assignment to temporary value
                    return;
                CFGVar newVar = new CFGVar(storedVar, storedVar.type());
                a.setVar(newVar);
                varMap.replace(storedVar.name(), newVar);
                maxVer.replace(newVar.name(), newVar);
                actives.add(newVar);
                break;
            case CFGPrint p:
                p.setVal((CFGValue) p.val().toSSA(varMap));
                break;
            case CFGSet s:
                s.setAddr((CFGVar)s.addr().toSSA(varMap));
                s.setIndex((CFGValue)s.index().toSSA(varMap));
                s.setVal((CFGData)s.val().toSSA(varMap));
                break;
            case CFGStore s:
                s.setIndex((CFGData)s.index().toSSA(varMap));
                s.setBase((CFGVar)s.base().toSSA(varMap));
                break;
            default:
                break;
        }
    }

    void jumpToSSA(HashMap<String, CFGVar> varMap) {
        switch(jmp) {
            case CFGCondOp c:
                c.setCond((CFGValue)c.cond().toSSA(varMap));
                break;
            case CFGRetOp r:
                r.setVal((CFGValue)r.val().toSSA(varMap));
            default:
                break;
        }
    }
    
    
    //set identifier (name) of a block
    private void setIdentifier(String blockBaseName) {
        this.identifier = blockBaseName + (blockId > 0 ? blockId : "");
        blockId++;
    }

    //sets predecessors and active variable lists of a block
    private void setPredsActives(HashSet<BasicBlock> preds, HashSet<CFGVar> actives) {
        this.preds.clear();
        this.succs.clear();
        this.actives.clear();
        this.preds.addAll(preds);
        if(preds.size() == 0) //if this is the starting pt of the method
            this.actives.addAll(actives);
        else
            this.actives.addAll(preds.iterator().next().getActives());
        for(BasicBlock p : this.preds) {
            p.succs.add(this);
        }
    }

    public void findNearestDominator() {
        ArrayDeque<BasicBlock> bfsQueue = new ArrayDeque<>();
        bfsQueue.add(this);
        BasicBlock curr;
        while (!bfsQueue.isEmpty()) {
            curr = bfsQueue.remove();
            if (curr != this && dominators.contains(curr)) {
                immediateDominator = curr;
                return;
            }
            for (BasicBlock p : curr.getPreds()) {
                bfsQueue.add(p);
            }
            
        }
        immediateDominator = null;
    }

    //print block as a String
    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        for(CFGAssn phi : phis)
            sb.append("\t" + phi + "\n");
        for(CFGOp op : ops) {
            sb.append("\t" + op + "\n");
        }
        sb.append("\t" + jmp);
        return sb.toString();
    }

    public HashSet<CFGVar> getActives() {
        return actives;
    }

    public void addSucc(BasicBlock b) {
        succs.add(b);
    }

    //overwrite existing actives and replace it with v - intended to be use to temporarily pre-initialize in cases where loops are being turned into CFG
    public void addActives(HashSet<CFGVar> v) {
        actives = v;
    }

    public void addOp(CFGOp c) {
        ops.add(c);
    }
    
    public void prependOp(CFGOp c) {
        ops.add(0, c);
    }

    private void addJump(CFGJumpOp j) {
        jmp = j;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void addPred(BasicBlock pred) {
        if(!preds.contains(pred)) {
            preds.add(pred);
            pred.addSucc(this);
        }
    }

    public HashSet<BasicBlock> getPreds() {
        return this.preds;
    }

    public HashSet<BasicBlock> getDominators() {
        return this.dominators;
    }

    public void setDominators(HashSet<BasicBlock> dominatorSet) {
        this.dominators = new HashSet<>();
        dominators.addAll(dominatorSet);
    }
    
    public void addDominator(BasicBlock b) {
        this.dominators.add(b);
    }

    //generate a pointer tag check for the variable var
    public void genPtrTagChk(CFGVar obj, ArrayList<BasicBlock> blocksInMethod, String blockBaseName, HashSet<BasicBlock> preds) {
        HashSet<BasicBlock> localPreds = new HashSet<>(preds);
        if(CtrlFlowGraph.isTyped())
            return;
        if(obj.isThis())
            return; //PEEPHOLE OPT - DONT GENERATE TAG CHECKS FOR THIS
        CFGVar objAddr = CFGVar.makeTmpVar(null);
        currBlock.addOp(new CFGAssn(objAddr, new CFGBinOp(obj, "&", CFGPrimitive.getPrimitive(1)))); // get LSB
        localPreds.add(currBlock);
        BasicBlock beforeBranch = currBlock;
        BasicBlock notPtr = new BasicBlock(blocksInMethod, CFGFailOpt.NotAPointer, localPreds); // basic block for not a ptr
        BasicBlock isPtr = new BasicBlock(blocksInMethod, localPreds, actives); // basic block for ptr is real
        beforeBranch.jmp = new CFGCondOp(beforeBranch, objAddr, notPtr, isPtr);
        isPtr.setIdentifier(blockBaseName);
    }

    public void genIntTagChk(CFGVar var, ArrayList<BasicBlock> blocksInMethod, String blockBaseName, HashSet<BasicBlock> localPreds){
        if(CtrlFlowGraph.isTyped())
            return;
        if(var.isThis())
            return; //PEEPHOLE OPT - DONT GENERATE TAG CHECKS FOR THIS
        CFGVar intOrPtr = CFGVar.makeTmpVar(DataType.intType);
        currBlock.addOp(new CFGAssn(intOrPtr, new CFGBinOp(var, "&", CFGPrimitive.getPrimitive(1)))); // if intOrPtr=1, we have int. else, we have ptr.
        localPreds.add(currBlock);
        BasicBlock branchBlock = currBlock;
        BasicBlock nanFail = new BasicBlock(blocksInMethod, CFGFailOpt.NotANumber, localPreds);
        BasicBlock isInt = new BasicBlock(blocksInMethod, localPreds, actives);
        isInt.setIdentifier(blockBaseName);
        branchBlock.addJump(new CFGCondOp(branchBlock, intOrPtr, isInt, nanFail));
    }

    //tag constant integers as they're being assigned to variables - detagged when printing
    public CFGExpr tagInts(CFGVar out, CFGExpr expr) {
        if(CtrlFlowGraph.isTyped())
            return expr;
        switch (expr) {
            case CFGPrimitive c:
                return tagInt(out, c);
            case CFGVar v:
                return v.shouldTag() ? tagInt(out, v) : v;
            case CFGBinOp b:
                return new CFGBinOp((CFGValue)tagInts(null, b.lhs()), b.op(), (CFGValue)tagInts(null, b.rhs()));
            default: //anything other than a const or a binop
                return expr;
        }
    }

    public CFGExpr tagInt(CFGVar out, CFGValue val) {
        if(CtrlFlowGraph.isTyped())
            return val;
        CFGVar lsft = CFGVar.makeTmpVar(DataType.intType);
        currBlock.addOp(new CFGAssn(lsft, new CFGBinOp(val, "<<", CFGPrimitive.getPrimitive(1))));
        CFGExpr incr = new CFGBinOp(lsft, "+", CFGPrimitive.getPrimitive(1));
        if (out == null) {
            CFGVar lsftPlus = CFGVar.makeTmpVar(DataType.intType);
            currBlock.addOp(new CFGAssn(lsftPlus, incr));
            return lsftPlus;
        }
        return incr;
    }

    //given an int var in, generate code to tag check and untag the int, returning the untagged variable out
    //if out is null, return a new temp - otherwise, assign the value to out and return it
    public CFGVar checkUntagInt(CFGVar out, CFGVar in, ArrayList<BasicBlock> blocksInMethod, String blockBaseName, HashSet<BasicBlock> localPreds) {
        genIntTagChk(in, blocksInMethod, blockBaseName, localPreds);
        localPreds.clear();
        return doUntag(out, in);
    }

    public CFGVar doUntag(CFGVar out, CFGVar in) {
        if(CtrlFlowGraph.isTyped())
            return in;
        CFGVar minus1 = CFGVar.makeTmpVar(DataType.intType);
        currBlock.addOp(new CFGAssn(minus1, new CFGBinOp(in, "-", CFGPrimitive.getPrimitive(1))));
        if(out == null) {
            out = CFGVar.makeTmpVar(DataType.intType);
        }
        currBlock.addOp(new CFGAssn(out, new CFGBinOp(minus1, ">>", CFGPrimitive.getPrimitive(1))));
        return out;
    }

    //convert a potentially complex CFG expr into a series of statements
    public CFGExpr exprToCFG(CFGVar assn, ArrayList<BasicBlock> blocksInMethod, String blockBaseName, ASTExpression expr, CFGVar[] locals, boolean requireVal) {
        HashSet<BasicBlock> localPreds = new HashSet<>();
        BasicBlock badFieldBlock, badMethodBlock;
        CFGExpr out;
        switch (expr) {
            case ASTConstant c:
                return CFGPrimitive.getPrimitive(c.value());
            case ASTNullExpr n:
                return CFGPrimitive.getPrimitive(0);
            case ASTVariable v:
                CFGVar tmpVar = getActive(v.name());
                if(tmpVar == null)
                    throw new IllegalArgumentException("Attempted to access nonexistent or uninitialized variable "+v.name() + " (expr "+expr+")");
                return tmpVar;
            case ASTBinop b:
                CFGExpr lhs, rhs;
                lhs = exprToCFG(null, blocksInMethod, blockBaseName, b.lhs(), locals, true); 
                rhs = exprToCFG(null, blocksInMethod, blockBaseName, b.rhs(), locals, true);
                if(lhs instanceof CFGVar) {
                    if(!b.isBool())
                        lhs = checkUntagInt(null, (CFGVar)lhs, blocksInMethod, blockBaseName, localPreds);
                    else
                        lhs = doUntag(null, (CFGVar)lhs);
                }
                if(rhs instanceof CFGVar) {
                    if(!b.isBool())
                        rhs = checkUntagInt(null, (CFGVar)rhs, blocksInMethod, blockBaseName, localPreds);
                    else
                        rhs = doUntag(null, (CFGVar)rhs);
                
                }
                CFGVar tmp;
                if(lhs instanceof CFGBinOp) {
                    tmp = CFGVar.makeTmpVar(null);
                    currBlock.addOp(new CFGAssn(tmp, lhs));
                    lhs = tmp;
                }
                if(rhs instanceof CFGBinOp) {
                    tmp = CFGVar.makeTmpVar(null);
                    currBlock.addOp(new CFGAssn(tmp, rhs));
                    rhs = tmp;
                }

                out = new CFGBinOp((CFGValue)lhs, b.op(), (CFGValue)rhs);
                if(!b.isBool()) {
                    CFGVar pretag = CFGVar.makeTmpVar(null);
                    currBlock.addOp(new CFGAssn(pretag, out));
                    out = tagInt(assn, pretag);
                }
                break;
            case ASTClassRef c: //used for class reference in a complex expression, so we need to return an anonymous(temp) value
                CFGClass classData = CtrlFlowGraph.findClass(c.classname());
                CFGVar cRef = assn;
                if(classData == null)
                    throw new IllegalArgumentException("Class "+c.classname()+" is undefined");
                if(cRef == null) {
                    cRef = CFGVar.makeTmpVar(null);
                    actives.add(cRef);
                }
                currBlock.addOp(new CFGAssn(cRef, new CFGAlloc(CFGPrimitive.getPrimitive(classData.numFields()+(CtrlFlowGraph.isTyped()?1:2))))); //alloc vtable, field map, fields
                currBlock.addOp(new CFGStore(cRef, classData.vtable()));
                if(!CtrlFlowGraph.isTyped()) {
                    CFGVar fieldsAddr = CFGVar.makeTmpVar(null);
                    currBlock.addOp(new CFGAssn(fieldsAddr, new CFGBinOp(cRef, "+", CFGPrimitive.getPrimitive(8))));
                    currBlock.addOp(new CFGStore(fieldsAddr, classData.fields()));
                }
                out = cRef;
                break;
            case ASTFieldRead f:
                int fieldId = CtrlFlowGraph.getFieldId(f.fieldname());
                if(fieldId == -1)
                    throw new IllegalArgumentException("Code attempts to read from never-defined field "+f.fieldname());
                CFGVar obj = (CFGVar)exprToCFG(null, blocksInMethod, blockBaseName, f.base(), locals, true), field;
                genPtrTagChk(obj, blocksInMethod, blockBaseName, localPreds); //gen tag check for obj
                BasicBlock getFieldId = currBlock;
                if(!CtrlFlowGraph.isTyped()) {
                    CFGVar fieldAddr = CFGVar.makeTmpVar(null);
                    getFieldId.addOp(new CFGAssn(fieldAddr, new CFGBinOp(obj, "+", CFGPrimitive.getPrimitive(8))));
                    CFGVar fieldMap = CFGVar.makeTmpVar(null);
                    getFieldId.addOp(new CFGAssn(fieldMap, new CFGLoad(fieldAddr)));
                    CFGVar fieldOffset = CFGVar.makeTmpVar(null); // used to get the offset of the field
                    getFieldId.addOp(
                            new CFGAssn(fieldOffset, new CFGGet(fieldMap, CFGPrimitive.getPrimitive(fieldId))));
                    // check if field actually exists
                    localPreds.remove(this);
                    localPreds.add(getFieldId);
                    badFieldBlock = new BasicBlock(blocksInMethod, CFGFailOpt.NoSuchField, localPreds);
                    BasicBlock getField = new BasicBlock(blocksInMethod, localPreds, actives);
                    getField.setIdentifier(blockBaseName);
                    getFieldId.addJump(new CFGCondOp(getFieldId, fieldOffset, getField, badFieldBlock));
                    field = CFGVar.makeTmpVar(null);
                    currBlock.addOp(new CFGAssn(field, new CFGGet(obj, fieldOffset)));
                    
                }
                else {
                    CFGVar offset = CFGVar.makeTmpVar(null);
                    CFGClass cl = CtrlFlowGraph.findClass(obj.type().typeName());
                    fieldId = cl.getFieldId(f.fieldname());
                    currBlock.addOp(new CFGAssn(offset, new CFGBinOp(obj, "+", CFGPrimitive.getPrimitive(8*fieldId))));
                    field = CFGVar.makeTmpVar(null);
                    currBlock.addOp(new CFGAssn(field, new CFGLoad(offset)));
                }
                out = field;
                break;
            case ASTMethodCall m:
                int methodId = CtrlFlowGraph.getMethodId(m.methodname());
                if(methodId == -1)
                    throw new IllegalArgumentException("Attempt to call nonexistent method"+m.methodname());
                obj = (CFGVar)exprToCFG(null, blocksInMethod, blockBaseName, m.base(), locals, true);
                genPtrTagChk(obj, blocksInMethod, blockBaseName, localPreds); //gen tag check for obj
                BasicBlock getMethodId = currBlock;
                //load vtable, find method
                CFGVar vtbl = CFGVar.makeTmpVar(null);
                getMethodId.addOp(new CFGAssn(vtbl, new CFGLoad(obj)));
                CFGVar methodAddr = CFGVar.makeTmpVar(null);
                getMethodId.addOp(new CFGAssn(methodAddr, new CFGGet(vtbl, CFGPrimitive.getPrimitive(methodId)))); //get vtable id
                if (!CtrlFlowGraph.isTyped()) {
                    localPreds.add(getMethodId);
                    badMethodBlock = new BasicBlock(blocksInMethod, CFGFailOpt.NoSuchMethod, localPreds);
                    BasicBlock callBlock = new BasicBlock(blocksInMethod, localPreds, actives);
                    callBlock.setIdentifier(blockBaseName);
                    getMethodId.jmp = new CFGCondOp(getMethodId, methodAddr, callBlock, badMethodBlock);
                    localPreds.clear();
                }
                CFGVar callRslt = CFGVar.makeTmpVar(null);
                CFGValue[] args = new CFGValue[m.args().size()];
                for(int i = 0; i < args.length; i++) {
                    ASTExpression e = m.args().get(i);
                    args[i] = (CFGValue)exprToCFG(null, blocksInMethod, blockBaseName, e, locals, true);
                    if(args[i] instanceof CFGPrimitive)
                        args[i] = (CFGValue)tagInt(null, args[i]);
                }

                currBlock.addOp(new CFGAssn(callRslt, new CFGCall(methodAddr, obj, args))); //figure out receiver
                out = callRslt;
                break;
            case ASTThisExpr t:
                return getActive("this");
            default:
                return null;
        }
        if(requireVal && !(out instanceof CFGVar)) {
            CFGVar tmp = CFGVar.makeTmpVar(null);
            currBlock.addOp(new CFGAssn(tmp, out));
            out = tmp;
        }
        return out;
    }

    //determine if a name corresponds with an active variable
    //returns the variable if one exists and null otherwise
    public CFGVar getActive(String varName) {
        for(CFGVar v : actives) {
            if(v.name().equals(varName)) {
                return v;
            }
        }
        return null;
    }
}



