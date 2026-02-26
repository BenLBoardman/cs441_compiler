package cfg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map.Entry;

import parser.*;
import util.DataType;
import cfg.op.*;
import cfg.expr.*;
import cfg.expr.data.*;

public class CtrlFlowGraph {
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
        CFGVar.resetTmp();
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
        BasicBlock start = new BasicBlock(blocksInMethod, m.name()+classname, m.body(), 0, activeVars, new HashSet<>(), locals, null);
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
