package parser.statement;

import java.util.HashMap;

import parser.ASTClass;
import util.DataType;

public sealed interface ASTStatement
    permits ASTAssignStmt, ASTVoidStmt, ASTFieldWriteStmt, ASTIfElseStmt, ASTIfOnlyStmt, ASTWhileStmt, ASTReturnStmt, ASTPrintStmt {
        public void checkTypes(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols);
}
