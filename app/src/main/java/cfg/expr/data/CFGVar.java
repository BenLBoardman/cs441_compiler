package cfg.expr.data;

import java.util.HashMap;

import cfg.expr.CFGExpr;
import util.DataType;

public class CFGVar extends CFGValue {
    private final String name;
    private final int version;
    private final DataType type;
    private static CFGVar tmp;
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

    public static CFGVar makeTmpVar(DataType type) {
        tmp = new CFGVar(tmp, type);
        return tmp;
    }

    public static void resetTmp() {
        tmp = new CFGVar("", null);
    }

    //public CFGVar(CFGVar prev) {
    //    this(prev.name, prev.version + 1, prev.type);
    //}

    //create a CFGVar with previous-based numbering and a specified data type - should only be used for temps!
    public CFGVar(CFGVar prev, DataType type) {
        this(prev.name, prev.version + 1, type);
    }

    //create a CFGVar with no type - should only be used for the first temp!
    //public CFGVar(String name) {
    //    this(name, null);
    //}

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

    @Override
    public CFGExpr toSSA(HashMap<String, CFGVar> varMap) {
        if(name.equals("") || name.equals("this"))
                    return this;
                return varMap.get(name);
    }
}
