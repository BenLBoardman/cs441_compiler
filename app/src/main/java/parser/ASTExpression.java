package parser;

import java.util.HashMap;

import util.DataType;

public sealed interface ASTExpression 
    permits ASTConstant, ASTBinop, ASTMethodCall, ASTFieldRead, ASTClassRef, ASTThisExpr, ASTVariable, ASTNullExpr {
        public DataType getType(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols);
}
