package parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import util.DataType;

public record ASTMethod(String name, String classname, HashMap<String, DataType> args, DataType returnType, HashMap<String, DataType> locals,
        ArrayList<Statement> body) {
    @Override
    public boolean equals(Object o) {
        return this.name.equals(((ASTMethod) o).name());
    }

    public void checkTypes(HashMap<String, ASTClass> types) {
        HashMap<String, DataType> symbolTable = new HashMap<>(args);
        symbolTable.putAll(locals);
        if(types.get(returnType.typeName()) == null)
            throw new IllegalArgumentException("Data type for method "+name+" is not defined in code.");
        for (Entry<String, DataType> a : symbolTable.entrySet()) {
            if (types.get(a.getValue().typeName()) == null)
                throw new IllegalArgumentException("Error: Data type for " + a.getKey() + " in method " + name()
                        + " is never declared in code.");
        }
        //do expr checking pass here
        for(Statement s : body) {
            s.checkTypes(types, symbolTable);
        }
    }
}
