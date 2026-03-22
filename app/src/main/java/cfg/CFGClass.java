package cfg;

import java.util.ArrayList;

import cfg.expr.data.CFGArray;
import cfg.expr.data.CFGPrimitive;
import cfg.expr.data.CFGVar;

public record CFGClass(String name, ArrayList<CFGVar> fields, CFGArray vtable, int numFields, ArrayList<CFGMethod> methods) {
    @Override public String toString() { 
        StringBuilder sb = new StringBuilder();
        for(CFGMethod m : methods) {
            sb.append(m).append('\n');
        }
        return sb.toString();
    }

    public int getFieldId(String fieldname) {
        for(int i = 0; i < fields.size(); i++) {
            if(fields.get(i).name().equals(fieldname))
                return i;
        }
        return -1;
    }

    public CFGPrimitive getBitMap() {
        String bits = "0";
        for(CFGVar f : fields) {
            bits = bits + (f.type().isObject() ? "1" : "0");
        }
        return CFGPrimitive.getPrimitive(Long.parseLong(bits, 2));
    }
}

