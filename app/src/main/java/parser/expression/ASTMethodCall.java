package parser.expression;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import parser.ASTClass;
import parser.ASTMethod;

import java.util.Iterator;

import util.DataType;

public record ASTMethodCall(ASTExpression base, String methodname, List<ASTExpression> args) implements ASTExpression {

    @Override
    public DataType getType(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols) {
        DataType baseType = base.getType(types, symbols), passedType, expectedType;
        ASTClass classRef = types.get(baseType.typeName());
        ASTMethod method;
        Iterator<Entry<String,DataType>> argIterator;
        Entry<String, DataType> argEntry;
        if(classRef == null)
            throw new IllegalArgumentException("Error: field read references invalid class "+baseType);
        method = classRef.methods().get(methodname);
        if(method == null)
            throw new IllegalArgumentException("Error: Attempt to call nonexistent method "+methodname+" of class "+classRef.name());
        argIterator = method.args().entrySet().iterator();
        for(int i = 0; i < args.size(); i++) {
            if(!argIterator.hasNext())
                throw new IllegalArgumentException("Argument count mismatch calling method "+methodname);
            argEntry = argIterator.next();
            passedType = args.get(i).getType(types, symbols);
            expectedType = argEntry.getValue();
            if(!passedType.equals(expectedType)) 
                throw new IllegalArgumentException("Argument "+i+" of method "+methodname+" should have type "+expectedType+", has actual type "+passedType);
        }
        if(argIterator.hasNext())
            throw new IllegalArgumentException("Argument count mismatch calling method "+methodname);
        return method.returnType();
    }}
