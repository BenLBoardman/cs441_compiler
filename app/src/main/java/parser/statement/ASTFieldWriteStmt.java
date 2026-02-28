package parser.statement;

import java.util.HashMap;

import parser.ASTClass;
import parser.expression.ASTExpression;
import util.DataType;

public record ASTFieldWriteStmt(ASTExpression base, String fieldname, ASTExpression rhs) implements ASTStatement{

    @Override
    public void checkTypes(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) {
        DataType baseType = base.getType(types, symbols);
        ASTClass classRef = types.get(baseType.typeName());
        DataType fieldType, writeType;
        if(classRef == null)
            throw new IllegalArgumentException("Error: field write references invalid class "+baseType.typeName());
        fieldType = classRef.fields().get(fieldname);
        if(fieldType == null)
            throw new IllegalArgumentException("Error: Attempt to write to nonexistent field "+fieldname+" of class "+classRef.name());
        writeType = rhs.getType(types, symbols);
        if(!fieldType.equals(writeType))
            throw new IllegalArgumentException("Error: type mismatch: Attempt to write to field of type "+fieldType+" with data of type "+writeType+" ("+baseType+'.'+fieldname+")");
    }}
