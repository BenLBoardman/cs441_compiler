package parser.expression;

import java.util.HashMap;

import parser.ASTClass;
import util.DataType;
import util.error.ErrorAccumulator;
import util.error.UndefinedClassError;

public record ASTFieldRead(ASTExpression base, String fieldname) implements ASTExpression {

    @Override
    public DataType getType(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) {
        DataType baseType = base.getType(types, symbols);
        ASTClass classRef = types.get(baseType.typeName());
        DataType fieldType;
        if(classRef == null || classRef.type() == DataType.errType || classRef.type() == DataType.intType) {
            ErrorAccumulator.addError(new UndefinedClassError(0, baseType));
            return DataType.errType;
        }
        fieldType = classRef.fields().get(fieldname);
        if(fieldType == null)
            throw new IllegalArgumentException("Error: Attempt to read nonexistent field "+fieldname+" of class "+classRef.name());
        return fieldType;
    }}
