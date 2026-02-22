package parser;

public sealed interface Statement
    permits AssignStmt, VoidStmt, FieldWriteStmt, IfElseStmt, IfOnlyStmt, WhileStmt, ReturnStmt, PrintStmt {
}
