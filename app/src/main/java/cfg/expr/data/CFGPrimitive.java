package cfg.expr.data;

import java.util.HashMap;

public class CFGPrimitive extends CFGValue { 
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
