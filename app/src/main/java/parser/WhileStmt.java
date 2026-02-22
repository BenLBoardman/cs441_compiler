package parser;

import java.util.ArrayList;

public record WhileStmt(Expression cond, ArrayList<Statement> body) implements Statement{}
