package parser;

import java.util.HashMap;
import util.DataType;

public sealed interface Statement
    permits AssignStmt, VoidStmt, FieldWriteStmt, IfElseStmt, IfOnlyStmt, WhileStmt, ReturnStmt, PrintStmt {
        public void checkTypes(HashMap<String, ASTClass> types, HashMap<String, DataType> symbols);
}
