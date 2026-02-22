package parser;

import java.util.HashMap;
import java.util.Map.Entry;

import util.DataType;

public record ParserClass(String name, HashMap<String, DataType> fields, DataType type, HashMap<Method, DataType> methods) {
    @Override
    public boolean equals(Object o) {
        return this.name.equals(((String) o));
    }

    public Iterable<Entry<Method, DataType>> iterMethodSet() {
        return methods.entrySet();
    }

    public Iterable<Method> iterMethods() {
        return methods.keySet();
    }
}
