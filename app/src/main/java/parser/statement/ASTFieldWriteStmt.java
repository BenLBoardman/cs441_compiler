package parser.statement;

import java.util.HashMap;

import parser.ASTClass;
import parser.expression.ASTExpression;
import util.DataType;
import util.error.ErrorAccumulator;
import util.error.UndefinedClassError;

public record ASTFieldWriteStmt(ASTExpression base, String fieldname, ASTExpression rhs) implements ASTStatement{

    @Override
    public void checkTypes(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) {
        DataType baseType = base.getType(types, symbols);
        ASTClass classRef = types.get(baseType.typeName());
        DataType fieldType, writeType;
        if(classRef == null || classRef.type() == DataType.errType || classRef.type() == DataType.intType) {
            ErrorAccumulator.addError(new UndefinedClassError(0, baseType));
            return;
        }
        fieldType = classRef.fields().get(fieldname);
        if(fieldType == null)
            throw new IllegalArgumentException("Error: Attempt to write to nonexistent field "+fieldname+" of class "+classRef.name());
        writeType = rhs.getType(types, symbols);
        if(!fieldType.equals(writeType))
            throw new IllegalArgumentException("Error: type mismatch: Attempt to write to field of type "+fieldType+" with data of type "+writeType+" ("+baseType+'.'+fieldname+")");
    }}
