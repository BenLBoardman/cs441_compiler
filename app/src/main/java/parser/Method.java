package parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import util.DataType;

public record Method(String name, HashMap<String, DataType> args, DataType returnType, HashMap<String, DataType> locals,
        ArrayList<Statement> body) {
    @Override
    public boolean equals(Object o) {
        return this.name.equals(((Method) o).name());
    }

    public void checkTypes(ArrayList<String> typeNames) {
        if(!typeNames.contains(returnType.typeName()))
            throw new IllegalArgumentException("Data type for method "+name+" is not defined in code.");
        for (Entry<String, DataType> a : args().entrySet()) {
            if (!typeNames.contains(a.getValue().typeName()))
                throw new IllegalArgumentException("Error: Data type for " + a.getKey() + " in method " + name()
                        + " is never declared in code.");
        }
        for (Entry<String, DataType> l : locals().entrySet()) {
            if (!typeNames.contains(l.getValue().typeName()))
                throw new IllegalArgumentException("Error: Data type for " + l.getKey() + " in method " + name()
                        + " is never declared in code.");
        }
    }
}
