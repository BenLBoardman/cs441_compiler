package parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import parser.statement.ASTStatement;
import util.DataType;
import util.error.ErrorAccumulator;
import util.error.TypeAnnotationError;

public record ASTMethod(String name, String classname, HashMap<String, DataType> args, DataType returnType, HashMap<String, DataType> locals,
        ArrayList<ASTStatement> body) {
    @Override
    public boolean equals(Object o) {
        return this.name.equals(((ASTMethod) o).name());
    }

    public void checkTypes(HashMap<String, ASTClass> types) {
        HashMap<String, DataType> symbolTable = new HashMap<>(args);
        symbolTable.putAll(locals);
        for (Entry<String, DataType> a : symbolTable.entrySet()) {
            if (types.get(a.getValue().typeName()) == null)
                ErrorAccumulator.addError(new TypeAnnotationError(0, a.getKey()));
        }
        //do expr checking pass here
        for(ASTStatement s : body) {
            s.checkTypes(types, symbolTable);
        }
    }
}
