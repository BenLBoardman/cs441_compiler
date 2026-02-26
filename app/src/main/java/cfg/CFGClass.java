package cfg;

import java.util.ArrayList;

import cfg.expr.data.CFGArray;

public record CFGClass(String name, CFGArray fields, ArrayList<String> fieldNames, CFGArray vtable, int numFields, ArrayList<CFGMethod> methods) {
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

