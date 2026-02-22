package parser;

import java.util.HashMap;

import util.DataType;

public sealed interface Expression 
    permits Constant, Binop, MethodCall, FieldRead, ClassRef, ThisExpr, Variable, NullExpr {
        public DataType getType(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols);
}
