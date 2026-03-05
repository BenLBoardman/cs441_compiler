package parser;

import java.util.HashMap;

import util.DataType;

public record ASTClass(String name, HashMap<String, DataType> fields, DataType type, HashMap<String, ASTMethod> methods) {
    @Override
    public boolean equals(Object o) {
        return this.name.equals(((String) o));
    }

    public Iterable<ASTMethod> iterMethods() {
        return methods.values();
    }
}
