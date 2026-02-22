package parser;

import java.util.HashMap;

import util.DataType;

public record FieldRead(Expression base, String fieldname) implements Expression {

    @Override
    public DataType getType(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) {
        DataType baseType = base.getType(types, symbols);
        ASTClass classRef = types.get(baseType.typeName());
        DataType fieldType;
        if(classRef == null)
            throw new IllegalArgumentException("Error: field read references invalid class "+baseType.typeName());
        fieldType = classRef.fields().get(fieldname);
        if(fieldType == null)
            throw new IllegalArgumentException("Error: Attempt to read nonexistent field "+fieldname+" of class "+classRef.name());
        return fieldType;
    }}
