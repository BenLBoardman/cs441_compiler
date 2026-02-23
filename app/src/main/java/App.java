import java.lang.StringBuilder;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import tokenize.*;
import util.DataType;
import parser.*;

import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;


sealed interface CFGElement 
    permits CFGOp, CFGJumpOp, CFGMethod, CFGExpr {}

sealed interface CFGOp extends CFGElement
    permits CFGAssn, CFGPrint, CFGSet, CFGStore {
}
non-sealed class CFGPrint implements CFGOp {
    private CFGValue val;
    
    public CFGPrint(CFGValue val) {
        this.val = val;
    }

    public CFGValue val() {
        return val;
    }

    public void setVal(CFGValue val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return "print(" + val + ")";
    }
}

non-sealed class CFGAssn implements CFGOp {
    private CFGVar var;
    private CFGExpr expr;
    
    @Override
    public String toString() {
        return var + " = " + expr;
    }

    public CFGVar var() {
        return var;
    }

    public void setVar(CFGVar var) {
        this.var = var;
    }

    public CFGExpr expr() {
        return expr;
    }

    public void setExpr(CFGExpr expr) {
        this.expr = expr;
    }

    public CFGAssn(CFGVar var, CFGExpr expr) {
        this.var = var;
        this.expr = expr;
    }
}

non-sealed class CFGStore implements CFGOp {
    private CFGVar base;
    private CFGData index;

    public CFGVar base() {
        return base;
    }

    public void setBase(CFGVar base) {
        this.base = base;
    }

    public CFGExpr index() {
        return index;
    }

    public void setIndex(CFGData i) {
        this.index = i;
    }

    public CFGStore(CFGVar base, CFGData i) {
        this.base = base;
        this.index = i;
    }

    @Override
    public String toString() {
        return "store(" + base + ", " + index + ")";
    }
}

non-sealed class CFGSet implements CFGOp{
    private CFGVar addr;
    private CFGValue index;
    private CFGData val;

    public CFGVar addr() {
        return addr;
    }

    public void setAddr(CFGVar addr) {
        this.addr = addr;
    }

    public CFGValue index() {
        return index;
    }

    public void setIndex(CFGValue index) {
        this.index = index;
    }

    public CFGData val() {
        return val;
    }

    public void setVal(CFGData val) {
        this.val = val;
    }

    public CFGSet(CFGVar addr, CFGValue index, CFGData val) {
        this.addr = addr;
        this.index = index;
        this.val = val;
    }



    @Override
    public String toString() {
        return "setelt(" + addr + ", " + index + ", " + val + ")";
    }
}

sealed interface CFGExpr extends CFGElement
    permits CFGData, CFGGet, CFGPhi, CFGCall, CFGAlloc, CFGLoad, CFGBinOp { public boolean equals(Object o); }

record CFGBinOp(CFGValue lhs, String op, CFGValue rhs) implements CFGExpr {
    @Override
    public String toString() {
        return lhs + " " + op + " " + rhs;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof CFGBinOp))
            return false;
        CFGBinOp b = (CFGBinOp)o;
        return this.lhs == b.lhs && this.op == b.op && this.rhs == b.rhs;
    }

    public CFGExpr evalBinOp() {
        if (lhs instanceof CFGPrimitive && rhs instanceof CFGPrimitive) { // optimize out double-constant binops
            CFGPrimitive lprim = (CFGPrimitive) lhs;
            CFGPrimitive rprim = (CFGPrimitive) rhs;
            long rslt;
            switch (op) {
                case "+":
                    rslt = lprim.value() + rprim.value();
                    break;
                case "-":
                    rslt = lprim.value() - rprim.value();
                    break;
                case "*":
                    rslt = lprim.value() * rprim.value();
                    break;
                case "/":
                    rslt = lprim.value() / rprim.value();
                    break;
                case ">":
                    rslt = lprim.value() > rprim.value() ? 1 : 0;
                    break;
                case "<":
                    rslt = lprim.value() < rprim.value() ? 1 : 0;
                    break;
                case "<<":
                    rslt = lprim.value() << rprim.value();
                    break;
                case ">>":
                    rslt = lprim.value() >> rprim.value();
                    break;
                case "<=":
                    rslt = lprim.value() <= rprim.value() ? 1 : 0;
                    break;
                case ">=":
                    rslt = lprim.value() >= rprim.value() ? 1 : 0;
                    break;
                case "==":
                    rslt = lprim.value() == rprim.value() ? 1 : 0;
                    break;
                case "!=":rslt = lprim.value() != rprim.value() ? 1 : 0;
                    break;
                case "&": rslt = lprim.value() & rprim.value(); break;
                default: // should be unreachable
                    rslt = 0;
            }
            return CFGPrimitive.getPrimitive(rslt);
        }
        return this;
    }
}

record CFGGet(CFGVar arr, CFGValue val) implements CFGExpr {
    @Override
    public String toString() {
        return "getelt(" + arr + ", " + val + ")";
    }

    @Override public boolean equals(Object o) {
        if(!(o instanceof CFGGet))
            return false;
        CFGGet g = (CFGGet)o;
        return g.arr.equals(this.arr) && g.val.equals(this.val);
    }
}

record CFGAlloc(CFGPrimitive size) implements CFGExpr {
    @Override
    public String toString() {
        return "alloc(" + size + ")";
    }
}

record CFGCall(CFGVar addr, CFGVar receiver, CFGValue[] args) implements CFGExpr {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("call(");
        sb.append(addr + ", " + receiver);
        for(CFGValue a : args)
            sb.append(", ").append(a);
        return sb.append(')').toString();
    }
} 

record CFGLoad(CFGVar base) implements CFGExpr {
    @Override
    public String toString() {
        return "load(" + base + ")";
    }
    
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof CFGLoad))
            return false;
        CFGLoad l = (CFGLoad)o;
        return l.base.equals(this.base);
    } 
}

record CFGPhi(ArrayList<BasicBlock> blocks, ArrayList<CFGValue> varVersions) implements CFGExpr {
    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("phi(");
        for(int i = 0; i < blocks.size(); i++) {
            sb.append(blocks.get(i).getIdentifier()).append(", ");
            sb.append(varVersions.get(i)).append((i < blocks.size() - 1) ? ", " : "");
        }
        return sb.append(")").toString();
    }
}

sealed interface CFGData extends CFGExpr
    permits CFGValue, CFGArray {}

sealed interface CFGJumpOp extends CFGElement
    permits CFGAutoJumpOp, CFGRetOp, CFGCondOp, CFGFail {

}
record CFGAutoJumpOp(BasicBlock target) implements CFGJumpOp { @Override public String toString() { return "jump " + target.getIdentifier(); } }

non-sealed class CFGRetOp implements CFGJumpOp {
    private CFGValue val;

    public CFGRetOp(CFGValue val) {
        this.val = val;
    }

    public CFGValue val() {
        return val;
    }

    public void setVal(CFGValue val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return "ret " + val;
    }
}

non-sealed class CFGCondOp implements CFGJumpOp
{
    private CFGValue cond;
    private BasicBlock yes;
    private BasicBlock no;

    public CFGCondOp(CFGValue cond, BasicBlock yes, BasicBlock no) {
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

record CFGFail(CFGFailOpt fail) implements CFGJumpOp { @Override public String toString() {return "fail "+fail.name(); } }
enum CFGFailOpt {
    NotANumber,
    NotAPointer,
    NoSuchField,
    NoSuchMethod
}

sealed interface CFGValue extends CFGData
    permits CFGVar, CFGPrimitive {}
non-sealed class CFGVar implements CFGValue {
    private final String name;
    private final int version;
    private final DataType type;
    private boolean shldTag;

    public CFGVar(String name, int version, DataType type) {
        this.name = name;
        this.version = version;
        this.type = type;
        this.shldTag = false;
    }


    public boolean shouldTag() {
        return this.shldTag;
    }

    public void setShouldTag(boolean shldTag) {
        this.shldTag = shldTag;
    }

    public String name() {
        return name;
    }

    public int version() {
        return version;
    }

    public DataType type() {
        return type;
    }

    public CFGVar(CFGVar prev) {
        this(prev.name, prev.version + 1, prev.type);
    }

    //create a CFGVar with previous-based numbering and a specified data type - should only be used for temps!
    public CFGVar(CFGVar prev, DataType type) {
        this(prev.name, prev.version + 1, type);
    }

    //create a CFGVar with no type - should only be used for the first temp!
    public CFGVar(String name) {
        this(name, null);
    }

    public CFGVar(String name, DataType type) {
        this(name, -1, type);
    }

    public boolean isThis() {
        return name.equals("this");
    }

    public boolean isTmp() {
        return name.equals("");
    }

    @Override public boolean equals(Object o) {
        try {
            return name.equals(((CFGVar)o).name()) && version == ((CFGVar)o).version;
        } catch (Exception e) {
            return false; //comparing CFGVar and non-CFGVar
        }
    }

    @Override
    public final String toString() {
        if(name.equals("this"))
            return "%this";
        return "%"+this.name+(this.version>=0?this.version:"");
    }
}
non-sealed class CFGPrimitive implements CFGValue { 
    private static HashMap<Long, CFGPrimitive> primitives = new HashMap<>();
    private long value;

    private CFGPrimitive(long value) {
        this.value = value;
        primitives.put(value, this);
    }

    public static CFGPrimitive getPrimitive(long value)  {
        CFGPrimitive exst = primitives.get(value);
        if(exst == null)
            return new CFGPrimitive(value);
        return exst;
    }
    
    @Override public boolean equals(Object o) { return this == o; }
    
    @Override public String toString() {return ""+this.value; }

    public long value() {
        return this.value;
    } 
}

record CFGMethod(String name, CFGVar[] args, CFGVar[] locals, BasicBlock addr, ArrayList<BasicBlock> blocks, ArrayList<CFGVar> vars) implements CFGElement {
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
            App.debug(this.toString());
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

record CFGArray(String name, Object[] elems) implements CFGData { int size()  {return this.elems.length; } @Override public String toString() { return "@"+name;} }

record CFGClass(String name, CFGArray fields, ArrayList<String> fieldNames, CFGArray vtable, int numFields, ArrayList<CFGMethod> methods) {
    @Override public String toString() { 
        StringBuilder sb = new StringBuilder();
        for(CFGMethod m : methods) {
            sb.append(m).append('\n');
        }
        return sb.toString();
    }

    public int getFieldId(String fieldname) {
        return fieldNames.indexOf(fieldname)+1;
    }
}

class CtrlFlowGraph {
    public static ArrayList<BasicBlock> basicBlocks;
    public static DataBlock CFGDataBlock;
    public static ArrayList<String> globals;
    public static ArrayList<String> methods;
    public static CFGMethod main;
    public static ArrayList<CFGClass> classes;
    public static ParsedCode parsedCode;
    private static boolean isTyped;

    public CtrlFlowGraph() {
        //empty constructor
    }

    public void mkCfg (ParsedCode code, boolean isTyped) {
        classes = new ArrayList<>();
        parsedCode = code;
        CFGDataBlock = new DataBlock(new ArrayList<>());
        //setup fields and vtables
        CFGArray vtable;
        CFGArray fields;
        CtrlFlowGraph.isTyped = isTyped;
        globals = new ArrayList<>();
        methods = new ArrayList<>();
        ArrayList<String> uniqueFields = new ArrayList<>();
        ArrayList<ASTMethod> uniqueMethods = new ArrayList<>();
        for(ASTClass c : code.classes) { // find all unique field & method names
            for(String f : c.fields().keySet()) {
                if(!uniqueFields.contains(f)) {
                    uniqueFields.add(f);
                    globals.add(f);
                }
            }
            for(ASTMethod m : c.iterMethods()) {
                if(!uniqueMethods.contains(m)) 
                    uniqueMethods.add(m);
                    methods.add(m.name());
            }
            
        }
        
        for(ASTClass c : code.classes) { // build fields and vtables
            vtable = new CFGArray("vtbl"+c.name(), new String[uniqueMethods.size()]);
            for(int i = 0; i < uniqueMethods.size(); i++) {
                if(c.methods().containsValue(uniqueMethods.get(i))) {
                    vtable.elems()[i] = uniqueMethods.get(i).name() + c.name();
                }
                else {
                    vtable.elems()[i] = "0";
                }
            }

            fields = new CFGArray("fields"+c.name(), new Integer[uniqueFields.size()]);
            int numFields =  1;
            for(int i = 0; i < uniqueFields.size(); i++) {
                if(c.fields().containsKey(uniqueFields.get(i))) {
                    fields.elems()[i] = 1 + numFields;
                    numFields++;
                }
                else {
                    fields.elems()[i] = 0;
                }
                
            }
            CFGDataBlock.data().add(vtable);
            if(!CtrlFlowGraph.isTyped())
                CFGDataBlock.data().add(fields);
            classes.add(new CFGClass(c.name(), fields, new ArrayList<String>(c.fields().keySet()), vtable, c.fields().size(), new ArrayList<>()));
        }

        basicBlocks = new ArrayList<>();
        for(int i = 0; i < code.classes.size(); i++) {
            ASTClass c = code.classes.get(i);
            CFGClass cfgClass = classes.get(i);
            for(ASTMethod m : c.iterMethods()) {
                cfgClass.methods().add(methodToCfg(m, c.name(), c.type(), false));
            }
        }

        main = methodToCfg(code.main, "", null, true);

    }

    public static CFGClass findClass(String s) {
        for(CFGClass c : classes) {
            if(c.name().equals(s))
                return c;
        }
        return null;
    }

    public static boolean isTyped() {return isTyped;}
    
    private CFGMethod methodToCfg(ASTMethod m, String classname, DataType classType, boolean isMain) {
        CFGVar tmp = new CFGVar("");
        HashSet<CFGVar> activeVars = new HashSet<>();
        CFGVar[] args = new CFGVar[0];
        Iterator<Entry<String, DataType>> iterator;
        Entry<String, DataType> entry;
        if (!isMain) {
            iterator = m.args().entrySet().iterator();
            args = new CFGVar[m.args().size()+1];
            args[0] = new CFGVar("this", classType);
            for (int i = 1; i < args.length; i++) {
                entry = iterator.next();
                args[i] = new CFGVar(entry.getKey(), entry.getValue());
            }
            Collections.addAll(activeVars, args);
        }
        CFGVar[] locals = new CFGVar[0];
        locals = new CFGVar[m.locals().size()];
        iterator = m.locals().entrySet().iterator();
        for (int i = 0; i < locals.length; i++) {
            entry = iterator.next();
            locals[i] = new CFGVar(entry.getKey(), entry.getValue());
        }
        BasicBlock.blockId = 0;
        
        ArrayList<BasicBlock> blocksInMethod = new ArrayList<>();
        ArrayList <CFGVar> vars = new ArrayList<>(Arrays.asList(args));
        vars.addAll(Arrays.asList(locals));
        BasicBlock start = new BasicBlock(blocksInMethod, m.name()+classname, m.body(), 0, activeVars, new HashSet<>(), tmp, locals, null);
        return new CFGMethod(m.name()+classname, args, locals, start, blocksInMethod, vars);
    }

    public static int getFieldId(String fieldName) {
        for(int i = 0; i < CtrlFlowGraph.globals.size(); i++) {
            if(globals.get(i).equals(fieldName))
                return i;
        }
        return -1;
    }

    public static int getMethodId(String methodName) {
        for(int i = 0; i < CtrlFlowGraph.methods.size(); i++) {
            if(methods.get(i).equals(methodName))
                return i;
        }
        return -1;
    }

    public static CFGVar getActive(ArrayList<CFGVar> actives, String varName) {
        for(CFGVar v : actives) {
            if(v.name().equals(varName)) {
                return v;
            }
        }
        return null;
    }

    public void toSSA(boolean simple) {
        HashMap<String, CFGVar> varMap;
        varMap = new HashMap<>();
        for(CFGVar v : main.vars())
            varMap.put(v.name(), v);
        setDominators(main.blocks());
        if (simple)
            mkSimplePhis(main.blocks()); // insert temp phis - simple ver
        else
            mkPhis(main.blocks()); // insert temp phis
        for (BasicBlock b : main.blocks())
            b.toSSA(varMap, new HashMap<>(varMap));

        for(CFGClass c : classes) {
            for(CFGMethod m : c.methods()) {
                varMap = new HashMap<>();
                for(CFGVar v : m.vars())
                    varMap.put(v.name(), v);
                setDominators(m.blocks());
                if(simple)
                    mkSimplePhis(m.blocks());
                else
                    mkPhis(m.blocks()); //insert temp phis
                for(BasicBlock b : m.blocks())
                    b.toSSA(varMap, new HashMap<>(varMap));
            }
        }
    }

    //calculate dominators, inverse dominators, nearest dominator, and dominance frontier for a set of blocks
    private void setDominators(ArrayList<BasicBlock> blocks) {
        HashSet<BasicBlock> allBlocks = new HashSet<>(blocks), tempDoms;
        blocks.get(0).addDominator(blocks.get(0));
        boolean changed = true;
        for(int i = 1; i < blocks.size(); i++) {
            blocks.get(i).setDominators(allBlocks);
        }
        while(changed) {
            changed = false;
            for(int i = 1; i < blocks.size(); i++) {
                BasicBlock b = blocks.get(i);
                tempDoms = new HashSet<>();
                for(BasicBlock p : b.getPreds()) {
                    if(tempDoms.size() == 0)
                        tempDoms.addAll(p.getDominators()); //initialize temp dominators to the dominators of the first pred
                    else
                        tempDoms.retainAll(p.getDominators()); //continually intersect temp with each successive pred
                }
                tempDoms.add(b); //add curr block to its own dominators
                if(!tempDoms.equals(b.getDominators())) {
                    changed = true;
                    b.setDominators(tempDoms);
                }
            }
        }
        for(BasicBlock b : blocks) {
            b.findNearestDominator();
	    for(BasicBlock d : b.dominators) { //add inverse dominators, this is maybe necessary but I'm not sure
                d.inverseDominators.add(b);
            }
        }
	    calcDominanceFrontiers(blocks);
    }

    private void calcDominanceFrontiers(ArrayList<BasicBlock> blocks) {
        BasicBlock tmp;
        for (BasicBlock b : blocks) {
            if (b.getPreds().size() <= 1)
                continue;
            for (BasicBlock p : b.getPreds()) {
                tmp = p;
                while (tmp != b.immediateDominator) {
                    tmp.dominanceFrontier.add(b);
                    tmp = tmp.immediateDominator;
                }
            }
        }
    }

    private void mkPhis(ArrayList<BasicBlock> blocks) {
        HashSet<CFGVar> globals = new HashSet<>(); // variables read aacross basic block
        HashMap<CFGVar, ArrayList<BasicBlock>> varBlocks = new HashMap<>(); //key = variable, val = blocks where variable is assigned
        HashSet<CFGVar> varKill; //vars assigned locally in-block
        ArrayList<BasicBlock> workList; // blocks needing phi work
        for (BasicBlock b : blocks) { // (incomplete) initial pass
            varKill = new HashSet<>();
            for (CFGOp c : b.getOps()) {
                switch (c) {
                    case CFGAssn a:
                        CFGVar out = a.var();
                        switch (a.expr()) {
                            case CFGBinOp n:
                                if (n.lhs() instanceof CFGVar && !varKill.contains(n.lhs()))
                                    globals.add((CFGVar) n.lhs());
                                if (n.rhs() instanceof CFGVar && !varKill.contains(n.rhs()))
                                    globals.add((CFGVar) n.rhs());
                                break;
                            case CFGVar v:
                                if (!varKill.contains(v))
                                    globals.add(v);
                                break;
                            case CFGCall l:
                                if (!varKill.contains(l.addr()))
                                    globals.add(l.addr());
                                if (!varKill.contains(l.receiver()))
                                    globals.add(l.receiver());
                                for (CFGValue x : l.args()) {
                                    if (x instanceof CFGVar && !varKill.contains(x))
                                        globals.add((CFGVar) x);
                                }
                                break;
                            case CFGGet g:
                                if (!varKill.contains(g.arr()))
                                    globals.add(g.arr());
                                if (g.val() instanceof CFGVar && !varKill.contains(g.val()))
                                    globals.add((CFGVar) g.val());
                                break;
                            default:
                                break;
                        }
                        varKill.add(out);
                        ArrayList<BasicBlock> blocksOut = varBlocks.get(out);
                        if(out.name().equals(""))
                            continue;
                        else if (blocksOut == null)
                            varBlocks.put(out, new ArrayList<>(Arrays.asList(b)));
                        else if (!blocksOut.contains(b))
                            blocksOut.add(b);
                        break;
                    case CFGSet s:
                        if(!varKill.contains(s.addr()))
                            globals.add(s.addr());
                        if(s.index() instanceof CFGVar && !varKill.contains(s.index()))
                            globals.add((CFGVar)s.index());
                        if(s.val() instanceof CFGVar && !varKill.contains(s.val()))
                            globals.add((CFGVar)s.val());
                        break;
                    case CFGPrint p:
                        if(p.val() instanceof CFGVar && !varKill.contains(p.val()))
                            globals.add((CFGVar)p.val());
                        break;
                    case CFGStore st:
                        if(!varKill.contains(st.base()))
                            globals.add(st.base());
                        if(st.index() instanceof CFGVar && !varKill.contains(st.index()))
                            globals.add((CFGVar)st.index());
                        break;
                    default:
                        break;
                }
            }
        }
        for(CFGVar v : globals) {
            
            workList = varBlocks.get(v);
            if(v.name().equals("this") || v.name().equals("") || varBlocks.get(v) == null)
                continue; //if var is this, temp, or not written across multiple blocks
            for(int i = 0; i < workList.size(); i++) {
                BasicBlock b = workList.get(i);
                for(BasicBlock d : b.dominanceFrontier) {
                    if(!d.hasPhi(v)) {
                        d.addPhi(v);
                        if(!workList.contains(d))
                            workList.add(d);
                    }
                    //stuff
                }
            }
        }        
    }

    private void mkSimplePhis(ArrayList<BasicBlock> blocks) {
        for(BasicBlock b : blocks) {
            if(b.getPreds().size() > 1) {
                for(CFGVar v : b.getActives()) {
                    if(!(v.isThis() || v.isTmp()))
                        b.addPhi(v);
                }
            }
        }
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(CFGDataBlock);
        sb.append("code:\n\n");
        for(CFGClass c : classes) {
            sb.append(c).append('\n');
        }
        sb.append(main);
        return sb.toString();
    }

    public void localValueNumber() {
        for(BasicBlock b : main.blocks())
            b.doLocalValueNumbering();

        for(CFGClass c : classes)
            for(CFGMethod m : c.methods())
                for(BasicBlock b : m.blocks())
                    b.doLocalValueNumbering();
    }
    
    public void cleanBlocks() {
        main.condenseBlocks();

        for(CFGClass c : classes)
            for(CFGMethod m : c.methods())
                m.condenseBlocks();
    }
}

record DataBlock(ArrayList<CFGArray> data){
    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("data:\n");
        for(CFGArray arr : data) {
            sb.append("global array "+arr.name()+": { ");
            for(int i = 0; i < arr.size(); i++) {
                sb.append(arr.elems()[i].toString());
                if(i != arr.size() - 1) {
                    sb.append(",");
                }
                sb.append(" ");
            }
            sb.append("}\n");

        }


        return sb.toString();
    }

}

//identifier: name of the basic block
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
    private HashSet<BasicBlock> succs;
    
    
    public HashSet<BasicBlock> dominators; //blocks that dominate this block
    public HashSet<BasicBlock> inverseDominators; //blocks this block dominates
    public BasicBlock immediateDominator; //the "nearest" dominator in the CFG
    public HashSet<BasicBlock> dominanceFrontier; //blocks that *almost* dominate this block

    private static CFGVar tmp;
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
            for (CFGOp o : ops) {
                switch (o) {
                    case CFGAssn a:
                        CFGVar precalc, v = a.var();
                        CFGExpr expr = a.expr();
                        if (expr instanceof CFGBinOp) //evaluate binary op (if both primitives) - basically poor-man's constant propagation
                            expr = ((CFGBinOp) expr).evalBinOp();
                        a.setExpr(expr);
                        if (expr instanceof CFGBinOp || expr instanceof CFGLoad) {
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
        HashSet<BasicBlock> preds, CFGVar tmp, CFGVar[] locals, BasicBlock jmpBack) {
            this(blocksInMethod, tmp, preds, actives);
            this.setupBlock(blocksInMethod, blockBaseName, stmts, startIndex, locals, jmpBack);
        }
    
    public HashSet<BasicBlock> getSuccs() {
        return succs;
    }

    public BasicBlock(ArrayList<BasicBlock> blocksInMethod, CFGVar tmp) { //placeholder constructor to just initialize arraylists
        if(tmp != null)
            BasicBlock.tmp = tmp;
        inSSA = false;
        currBlock = this;
        blocksInMethod.add(this);
        CtrlFlowGraph.basicBlocks.add(this);
        preds = new HashSet<>();
        succs = new HashSet<>();
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
    public BasicBlock(ArrayList<BasicBlock> blocksInMethod, CFGVar tmp, HashSet<BasicBlock> preds, HashSet<CFGVar> actives) {
        this(blocksInMethod, tmp);
        setPredsActives(preds, actives);
    }

    //create a fail block
    public BasicBlock(ArrayList<BasicBlock> blocksInMethod, CFGVar tmp, CFGFailOpt failType, HashSet<BasicBlock> preds) {
        this(blocksInMethod, tmp);
        this.preds = new HashSet<>(preds);
        for(BasicBlock p : this.preds) {
            p.succs.add(this);
        }
        jmp = new CFGFail(failType);
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
        for(BasicBlock p : this.preds) {
            p.addSucc(this);
        }
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
                    if(!(operand instanceof CFGBinOp))
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
                    ifBlk = new BasicBlock(blocksInMethod, null);
                    if(i < stmts.size()-1) {
                        afterIf = new BasicBlock(blocksInMethod, null);
                        blocksInMethod.remove(blocksInMethod.indexOf(afterIf));
                    }
                    ifBlk.setPredsActives(localPreds, actives);
                    
                    currBlock = ifBlk;
                    ifBlk.setupBlock(blocksInMethod, blockBaseName, ie.body(), 0, locals, afterIf); 
                    BasicBlock endIf = currBlock;
                    BasicBlock elseBlk = new BasicBlock(blocksInMethod, null);
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
                    branchEntryBlock.jmp = new CFGCondOp(cond, ifBlk, elseBlk);
                    localPreds.clear();
                    return;
                case ASTIfOnlyStmt io:
                    cond = (CFGValue)exprToCFG(null, blocksInMethod, blockBaseName, io.cond(), locals, true);
                    if(cond instanceof CFGVar && !((CFGVar)cond).isTmp())
                        cond = checkUntagInt(null, (CFGVar)cond, blocksInMethod, blockBaseName, localPreds);
                    localPreds.add(currBlock);
                    branchEntryBlock = currBlock;
                    ifBlk = new BasicBlock(blocksInMethod, null);
                    ifBlk.setPredsActives(localPreds, actives);
                    currBlock = ifBlk;
                    ifBlk.setupBlock(blocksInMethod, blockBaseName, io.body(), 0, locals, afterIf);
                    afterIf = new BasicBlock(blocksInMethod, null);
                    afterIf.setPredsActives(localPreds, actives);
                    afterIf.setupBlock(blocksInMethod, blockBaseName, stmts, i+1, locals, jmpBack);
                    branchEntryBlock.jmp = new CFGCondOp(cond, ifBlk, afterIf);
                    localPreds.clear();
                    return;
                case ASTWhileStmt w:
                    branchEntryBlock = currBlock;
                    localPreds.add(branchEntryBlock);
                    BasicBlock loopheadStart = new BasicBlock(blocksInMethod, null);
                    loopheadStart.setPredsActives(localPreds, actives);
                    localPreds.remove(branchEntryBlock);
                    loopheadStart.setIdentifier(blockBaseName);
                    cond = (CFGValue)exprToCFG(null, blocksInMethod, blockBaseName, w.cond(), locals, true);
                    if(cond instanceof CFGVar && !((CFGVar)cond).isTmp())
                        cond = checkUntagInt(null, (CFGVar)cond, blocksInMethod, blockBaseName, localPreds);
                    BasicBlock loopheadEnd = currBlock;
                    localPreds.add(loopheadEnd);
                    loopheadEnd.addActives(actives);
                    BasicBlock body = new BasicBlock(blocksInMethod, blockBaseName, w.body(), 0, actives, localPreds, null, locals, loopheadStart);
                    currBlock.succs.add(loopheadStart);
                    BasicBlock after = new BasicBlock(blocksInMethod, blockBaseName, stmts , i + 1, actives, localPreds, null, locals, jmpBack);
                    localPreds.remove(loopheadEnd);
                    loopheadEnd.addJump(new CFGCondOp(cond, body, after)); //add jump at end
                    branchEntryBlock.jmp = new CFGAutoJumpOp(loopheadStart);  
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
                    tmp = new CFGVar(tmp);
                    CFGVar field;
                    if(!CtrlFlowGraph.isTyped()) {
                        CFGVar fieldsAddr = tmp;
                        tmp = new CFGVar(tmp);
                        CFGVar fields = tmp;   
                        getField.addOp(new CFGAssn(fieldsAddr, new CFGBinOp(obj, "+", CFGPrimitive.getPrimitive(8))));
                        getField.addOp(new CFGAssn(fields, new CFGLoad(fieldsAddr)));
                        tmp = new CFGVar(tmp);
                        field = tmp;
                        getField.addOp(new CFGAssn(field, new CFGGet(fields, CFGPrimitive.getPrimitive(fieldId))));
                        localPreds.remove(currBlock);
                        localPreds.add(getField);
                        BasicBlock fieldFail = new BasicBlock(blocksInMethod, null, CFGFailOpt.NoSuchField, localPreds);
                        BasicBlock storeVar = new BasicBlock(blocksInMethod, null, localPreds, actives);
                        getField.jmp = new CFGCondOp(field, storeVar, fieldFail); // test for no such field
                        if (objToStore instanceof CFGPrimitive)
                            objToStore = (CFGValue) tagInt(null, objToStore);
                        currBlock.addOp(new CFGSet(obj, field, objToStore));
                        currBlock.setupBlock(blocksInMethod, blockBaseName, stmts, i + 1, locals, jmpBack);
                        return; // rest of method is processed by setupBlock
                    } else {
                        CFGVar offset = tmp;
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
                    currBlock.jmp = new CFGRetOp(valToReturn);
                    break;
                case ASTVoidStmt v:
                    CFGValue voidRslt = (CFGValue)exprToCFG(null, blocksInMethod, blockBaseName, v.rhs(), locals, true);
                    tmp = new CFGVar(tmp);
                    currBlock.addOp(new CFGAssn(tmp, voidRslt));
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
                currBlock.jmp = new CFGRetOp(CFGPrimitive.getPrimitive(0));
            else {
                jmpBack.addPred(currBlock);
                currBlock.jmp = new CFGAutoJumpOp(jmpBack);
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
            CFGVar newVer = new CFGVar(oldVer);
            phi.setVar(newVer);
            varMap.replace(oldVer.name(), newVer);
            maxVer.replace(oldVer.name(), newVer);
        }
        for(CFGOp o : ops) {
            opToSSA(o, varMap, maxVer);
        }
        jumpToSSA(varMap);
        for(BasicBlock succ : succs) { //put all succs of this block that it also dominates into SSA
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
                succ.toSSA(outVars, maxVer);
            }
        }
    }

    void opToSSA (CFGOp o, HashMap<String, CFGVar> varMap, HashMap<String, CFGVar> maxVer) {
        switch (o) {
            case CFGAssn a:
                a.setExpr(exprToSSA(a.expr(), varMap));
                // do whatever thing needs to be added for exprs
                CFGVar base = a.var();
                CFGVar storedVar = varMap.get(base.name());
                if (storedVar == null) // assignment to temporary value
                    return;
                CFGVar newVar = new CFGVar(storedVar);
                a.setVar(newVar);
                varMap.replace(storedVar.name(), newVar);
                maxVer.replace(newVar.name(), newVar);
                actives.add(newVar);
                break;
            case CFGPrint p:
                p.setVal((CFGValue) exprToSSA(p.val(), varMap));
                break;
            case CFGSet s:
                s.setAddr((CFGVar) exprToSSA(s.addr(), varMap));
                s.setIndex((CFGValue) exprToSSA(s.index(), varMap));
                s.setVal((CFGData) exprToSSA(s.val(), varMap));
                break;
            case CFGStore s:
                s.setIndex((CFGData) exprToSSA(s.index(), varMap));
                s.setBase((CFGVar) exprToSSA(s.base(), varMap));
                break;
            default:
                break;
        }
    }

    void jumpToSSA(HashMap<String, CFGVar> varMap) {
        switch(jmp) {
            case CFGCondOp c:
                c.setCond((CFGValue)exprToSSA(c.cond(), varMap));
                break;
            case CFGRetOp r:
                r.setVal((CFGValue)exprToSSA(r.val(), varMap));
            default:
                break;
        }
    }
    
    CFGExpr exprToSSA(CFGExpr expr, HashMap<String, CFGVar> varMap) {
        switch(expr) {
            case CFGVar v:
                if(v.name().equals("") || v.name().equals("this"))
                    return expr;
                CFGVar currVer = varMap.get(v.name());
                return currVer;
            case CFGPrimitive c:
                return expr;
            case CFGBinOp b:
                CFGValue left = (CFGValue)exprToSSA(b.lhs(), varMap);
                CFGValue right = (CFGValue)exprToSSA(b.rhs(), varMap);
                CFGExpr currExpr = new CFGBinOp(left, b.op(), right);
                return currExpr;
            case CFGCall c:
                CFGVar newAddr = (CFGVar)exprToSSA(c.addr(), varMap);
                CFGVar newReceiver = (CFGVar)exprToSSA(c.receiver(), varMap);
                CFGValue[] newArgs = new CFGValue[c.args().length];
                for(int i = 0; i < newArgs.length; i++) {
                    CFGValue v = c.args()[i];
                    newArgs[i] = (CFGValue)exprToSSA(v, varMap);
                }
                return new CFGCall(newAddr, newReceiver, newArgs);
            case CFGGet g:
                CFGVar arr = (CFGVar)exprToSSA(g.arr(), varMap);
                CFGValue val = (CFGValue)exprToSSA(g.val(), varMap);
                return new CFGGet(arr, val);
            case CFGLoad l:
                CFGVar base = (CFGVar)exprToSSA(l.base(), varMap);
                return new CFGLoad(base);
            default:
                    return expr;
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
        tmp = new CFGVar(tmp);
        CFGVar objAddr = tmp;
        currBlock.addOp(new CFGAssn(tmp, new CFGBinOp(obj, "&", CFGPrimitive.getPrimitive(1)))); // get LSB
        localPreds.add(currBlock);
        BasicBlock beforeBranch = currBlock;
        BasicBlock notPtr = new BasicBlock(blocksInMethod, null, CFGFailOpt.NotAPointer, localPreds); // basic block for not a ptr
        BasicBlock isPtr = new BasicBlock(blocksInMethod, null, localPreds, actives); // basic block for ptr is real
        beforeBranch.jmp = new CFGCondOp(objAddr, notPtr, isPtr);
        isPtr.setIdentifier(blockBaseName);
    }

    public void genIntTagChk(CFGVar var, ArrayList<BasicBlock> blocksInMethod, String blockBaseName, HashSet<BasicBlock> localPreds){
        if(CtrlFlowGraph.isTyped())
            return;
        if(var.isThis())
            return; //PEEPHOLE OPT - DONT GENERATE TAG CHECKS FOR THIS
        tmp = new CFGVar(tmp);
        CFGVar intOrPtr = tmp;
        currBlock.addOp(new CFGAssn(intOrPtr, new CFGBinOp(var, "&", CFGPrimitive.getPrimitive(1)))); // if intOrPtr=1, we have int. else, we have ptr.
        localPreds.add(currBlock);
        BasicBlock branchBlock = currBlock;
        BasicBlock nanFail = new BasicBlock(blocksInMethod, tmp, CFGFailOpt.NotANumber, localPreds);
        BasicBlock isInt = new BasicBlock(blocksInMethod, intOrPtr, localPreds, actives);
        isInt.setIdentifier(blockBaseName);
        branchBlock.addJump(new CFGCondOp(intOrPtr, isInt, nanFail));
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
        tmp = new CFGVar(tmp);
        CFGVar lsft = tmp;
        currBlock.addOp(new CFGAssn(lsft, new CFGBinOp(val, "<<", CFGPrimitive.getPrimitive(1))));
        CFGExpr incr = new CFGBinOp(lsft, "+", CFGPrimitive.getPrimitive(1));
        if (out == null) {
            tmp = new CFGVar(tmp);
            CFGVar lsftPlus = tmp;
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
        tmp = new CFGVar(tmp);
        CFGVar minus1 = tmp;
        currBlock.addOp(new CFGAssn(minus1, new CFGBinOp(in, "-", CFGPrimitive.getPrimitive(1))));
        if(out == null) {
            tmp = new CFGVar(tmp);
            out = tmp;
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
                    throw new IllegalArgumentException("Attempted to access nonexistent or uninitialized variable "+v.name() + " (expr "+expr+")\n\nCURR CFG:\n\n"+App.cfg);
                return tmpVar;
            case ASTBinop b:
                CFGExpr lhs, rhs;
                lhs = exprToCFG(null, blocksInMethod, blockBaseName, b.lhs(), locals, true); 
                rhs = exprToCFG(null, blocksInMethod, blockBaseName, b.rhs(), locals, true);
                if(lhs instanceof CFGPrimitive && rhs instanceof CFGPrimitive) { //optimize out double-constant binops
                    CFGPrimitive lprim = (CFGPrimitive)lhs;
                    CFGPrimitive rprim = (CFGPrimitive)rhs;
                    long rslt;
                    switch (b.op()) {
                        case "+": rslt = lprim.value()+rprim.value(); break;
                        case "-": rslt = lprim.value()-rprim.value(); break;
                        case "*": rslt = lprim.value()*rprim.value(); break;
                        case "/": rslt = lprim.value()/rprim.value(); break;
                        case ">": rslt = lprim.value() > rprim.value() ? 1 : 0; break;
                        case "<": rslt = lprim.value() < rprim.value() ? 1 : 0; break;
                        case "<<": rslt = lprim.value()<<rprim.value(); break;
                        case ">>": rslt = lprim.value()>>rprim.value(); break;
                        case "<=": rslt = lprim.value()<=rprim.value() ? 1 : 0; break;
                        case ">=": rslt = lprim.value()>=rprim.value() ? 1 : 0; break;
                        case "==": rslt = lprim.value()==rprim.value() ? 1 : 0; break;
                        case "!=": rslt = lprim.value()!=rprim.value() ? 1 : 0; break;
                        default: //should be unreachable
                            rslt = 0;
                    }
                    return CFGPrimitive.getPrimitive(rslt);
                }
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
                if(lhs instanceof CFGBinOp) {
                    tmp = new CFGVar(tmp);
                    currBlock.addOp(new CFGAssn(tmp, lhs));
                    lhs = tmp;
                }
                if(rhs instanceof CFGBinOp) {
                    tmp = new CFGVar(tmp);
                    currBlock.addOp(new CFGAssn(tmp, rhs));
                    rhs = tmp;
                }

                out = new CFGBinOp((CFGValue)lhs, b.op(), (CFGValue)rhs);
                if(!b.isBool()) {
                    tmp = new CFGVar(tmp);
                    CFGVar pretag = tmp;
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
                    tmp = new CFGVar(tmp);
                    cRef = tmp;
                    actives.add(tmp);
                }
                currBlock.addOp(new CFGAssn(cRef, new CFGAlloc(CFGPrimitive.getPrimitive(classData.numFields()+(CtrlFlowGraph.isTyped()?1:2))))); //alloc vtable, field map, fields
                currBlock.addOp(new CFGStore(cRef, classData.vtable()));
                if(!CtrlFlowGraph.isTyped()) {
                    tmp = new CFGVar(tmp);
                    currBlock.addOp(new CFGAssn(tmp, new CFGBinOp(cRef, "+", CFGPrimitive.getPrimitive(8))));
                    currBlock.addOp(new CFGStore(tmp, classData.fields()));
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
                    tmp = new CFGVar(tmp);
                    CFGVar fieldAddr = tmp;
                    getFieldId.addOp(new CFGAssn(fieldAddr, new CFGBinOp(obj, "+", CFGPrimitive.getPrimitive(8))));
                    tmp = new CFGVar(tmp);
                    CFGVar fieldMap = tmp;
                    getFieldId.addOp(new CFGAssn(fieldMap, new CFGLoad(fieldAddr)));
                    tmp = new CFGVar(tmp);
                    CFGVar fieldOffset = tmp; // used to get the offset of the field
                    getFieldId.addOp(
                            new CFGAssn(fieldOffset, new CFGGet(fieldMap, CFGPrimitive.getPrimitive(fieldId))));
                    // check if field actually exists
                    localPreds.remove(this);
                    localPreds.add(getFieldId);
                    badFieldBlock = new BasicBlock(blocksInMethod, null, CFGFailOpt.NoSuchField, localPreds);
                    BasicBlock getField = new BasicBlock(blocksInMethod, null, localPreds, actives);
                    getField.setIdentifier(blockBaseName);
                    getFieldId.addJump(new CFGCondOp(fieldOffset, getField, badFieldBlock));
                    tmp = new CFGVar(tmp);
                    field = tmp;
                    currBlock.addOp(new CFGAssn(field, new CFGGet(obj, fieldOffset)));
                    
                }
                else {
                    tmp = new CFGVar(tmp);
                    CFGVar offset = tmp;
                    CFGClass cl = CtrlFlowGraph.findClass(obj.type().typeName());
                    fieldId = cl.getFieldId(f.fieldname());
                    currBlock.addOp(new CFGAssn(offset, new CFGBinOp(obj, "+", CFGPrimitive.getPrimitive(8*fieldId))));
                    tmp = new CFGVar(tmp);
                    field = tmp;
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
                tmp = new CFGVar(tmp);
                CFGVar vtbl = tmp;
                getMethodId.addOp(new CFGAssn(vtbl, new CFGLoad(obj)));
                tmp = new CFGVar(tmp);
                CFGVar methodAddr = tmp;
                getMethodId.addOp(new CFGAssn(methodAddr, new CFGGet(vtbl, CFGPrimitive.getPrimitive(methodId)))); //get vtable id
                if (!CtrlFlowGraph.isTyped()) {
                    localPreds.add(getMethodId);
                    badMethodBlock = new BasicBlock(blocksInMethod, null, CFGFailOpt.NoSuchMethod, localPreds);
                    BasicBlock callBlock = new BasicBlock(blocksInMethod, null, localPreds, actives);
                    callBlock.setIdentifier(blockBaseName);
                    getMethodId.jmp = new CFGCondOp(methodAddr, callBlock, badMethodBlock);
                    localPreds.clear();
                }
                tmp = new CFGVar(tmp);
                CFGVar callRslt = tmp;
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
            tmp = new CFGVar(tmp);
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



public class App {
    public static CtrlFlowGraph cfg;
    static boolean debug = false;
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: <comp> infile [-o outfile] [args...]");
            System.exit(1);
        }
        String inFilePath = args[0];
        String outFilePath = "";
        boolean ssa = true, outName = false, simple = false, vn = true, typed = true;
        if(args.length >= 3 && args[1].equals("-o")) {
            
        }

        for(int nextArg =  1; nextArg < args.length; nextArg++) {
            switch (args[nextArg]) {
                case "-noSSA":
                    ssa = false;
                    break;
                case "-simpleSSA":
                    simple = true;
                    break;
                case "-noVN":
                    vn = false;
                    break;
                case "-notype":
                    typed = false;
                    break;
                case "-o":
                    if(nextArg >= (args.length - 1))
                        throw new IllegalArgumentException("Error: received -o flag but no following arg to designate output file");
                    if(outName)
                        throw new IllegalArgumentException("Error: attempted to specify illegal second output file");
                    nextArg++;
                    outFilePath = args[nextArg];
                    outName = true;
                    break;
                case "-d":
                    debug = true;
                    break;
                default:
                    System.out.print("Command-line arg "+args[nextArg]+" not recognized");
                    System.exit(1);
            }
        }
        vn = vn && ssa;
        String code = "";
        try {
            code = Files.readString(Path.of("test-code/"+inFilePath), StandardCharsets.UTF_8);
        } catch(Exception e) {
            System.err.println("Failed to locate file "+inFilePath);
            System.exit(1);
        }
        Tokenizer tok = new Tokenizer(code);
        Parser p = new Parser(tok);
        
        ParsedCode pc = p.parse();
        cfg = new CtrlFlowGraph();
        cfg.mkCfg(pc, typed);
        if(ssa)
            cfg.toSSA(simple);
        if(vn)
            cfg.localValueNumber();
        cfg.cleanBlocks();
        if(outFilePath == "") {
            System.out.println(cfg);
            return;
        }
        try {
            Files.write(Path.of("test-out/"+outFilePath), cfg.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch(Exception e) {
            System.err.println("Cannot write code to file "+outFilePath);
            e.printStackTrace();
        }
    }

    public static void debug(String... str) {
        if(debug)
            System.out.println(str);
    }
}
