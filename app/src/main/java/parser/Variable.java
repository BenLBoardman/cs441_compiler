package parser;

import java.util.HashMap;

import util.DataType;

public record Variable(String name) implements Expression {

    @Override
    public DataType getType(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) {
        return symbols.get(name);
    }


}
