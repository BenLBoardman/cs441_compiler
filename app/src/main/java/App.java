import java.lang.StringBuilder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;


enum TokenType { 
    // Fixed punctuation
    LEFT_PAREN,
    RIGHT_PAREN,
    LEFT_BRACE,
    RIGHT_BRACE,
    LEFT_BRACK,
    RIGHT_BRACK,
    CARET,
    AMPERSAND,
    ATSIGN,
    NOT,
    DOT,
    COLON,
    COMMA,
    UNDERSCORE,
    // Keywords
    THIS,
    IF,
    IFONLY,
    ELSE,
    WHILE,
    RETURN,
    METHOD,
    CLASS,
    PRINT,
    FIELDS,
    WITH,
    LOCALS,
    MAIN,
    EOF,
    // Tokens with data
    OPERATOR,
    NUMBER,
    IDENTIFIER
}

sealed interface Token 
    permits Number, LeftParen, RightParen, Operator, Caret, Ampersand, AtSign, Not, Dot, Underscore, If, IfOnly, Else, While, Return, TMethod, TClass, Print, 
    Fields, With, Locals, Main, Colon, LeftBrace, RightBrace, LeftBrack, RightBrack, Identifier, Eof, This, Comma {
    TokenType getType();
}
record Number(long value) implements Token { @Override public TokenType getType() { return TokenType.NUMBER; } }
record LeftParen() implements Token { @Override public TokenType getType() { return TokenType.LEFT_PAREN; } }
record RightParen() implements Token { @Override public TokenType getType() { return TokenType.RIGHT_PAREN; } }
record LeftBrace() implements Token { @Override public TokenType getType() { return TokenType.LEFT_BRACE; } }
record RightBrace() implements Token { @Override public TokenType getType() { return TokenType.RIGHT_BRACE; } }
record LeftBrack() implements Token { @Override public TokenType getType() { return TokenType.LEFT_BRACK; } }
record RightBrack() implements Token { @Override public TokenType getType() { return TokenType.RIGHT_BRACK; } }
record Operator(String op) implements Token {
    @Override public TokenType getType() { return TokenType.OPERATOR; }
    public String getOp() { return this.op; }
}
record Caret() implements Token { @Override public TokenType getType() { return TokenType.CARET; } }
record Ampersand() implements Token { @Override public TokenType getType() { return TokenType.AMPERSAND; } }
record AtSign() implements Token { @Override public TokenType getType() { return TokenType.ATSIGN; } }
record Not() implements Token { @Override public TokenType getType() { return TokenType.NOT; } }
record Dot() implements Token { @Override public TokenType getType() { return TokenType.DOT; } }
record Underscore() implements Token { @Override public TokenType getType() { return TokenType.UNDERSCORE; } }
record If() implements Token { @Override public TokenType getType() { return TokenType.IF; } }
record IfOnly() implements Token { @Override public TokenType getType() { return TokenType.IFONLY; } }
record Else() implements Token { @Override public TokenType getType() { return TokenType.ELSE; } }
record While() implements Token { @Override public TokenType getType() { return TokenType.WHILE; } }
record Return() implements Token { @Override public TokenType getType() { return TokenType.RETURN; } }
record TMethod() implements Token { @Override public TokenType getType() { return TokenType.METHOD; } }
record TClass() implements Token { @Override public TokenType getType() { return TokenType.CLASS; } }
record Print() implements Token { @Override public TokenType getType() { return TokenType.PRINT; } }
record With() implements Token { @Override public TokenType getType() { return TokenType.WITH; } }
record Fields() implements Token { @Override public TokenType getType() { return TokenType.FIELDS; } }
record Main() implements Token { @Override public TokenType getType() { return TokenType.MAIN; } }
record Locals() implements Token { @Override public TokenType getType() { return TokenType.LOCALS; } }
record Colon() implements Token { @Override public TokenType getType() { return TokenType.COLON; } }
record Comma() implements Token { @Override public TokenType getType() { return TokenType.COMMA; } }
record Eof() implements Token { @Override public TokenType getType() { return TokenType.EOF; } }
record Identifier(String name) implements Token { @Override public TokenType getType() { return TokenType.IDENTIFIER; } }
record This() implements Token { @Override public TokenType getType() { return TokenType.THIS; } }

class Tokenizer {

    // We'll pre-allocate and reuse common tokens without data
    private final LeftParen lp = new LeftParen();
    private final RightParen rp = new RightParen();
    private final LeftBrace lb = new LeftBrace();
    private final RightBrace rb = new RightBrace();
    private final LeftBrack lbk = new LeftBrack();
    private final RightBrack rbk = new RightBrack();
    private final Colon colon = new Colon();
    private final TMethod method = new TMethod();
    private final TClass tClass = new TClass();
    private final Print print = new Print();
    private final With with = new With();
    private final Fields fields = new Fields();
    private final Main main = new Main();
    private final Locals locals = new Locals();
    private final Return ret = new Return();
    private final While w = new While();
    private final If iff = new If();
    private final IfOnly ifonly = new IfOnly();
    private final Else elseTok = new Else();
    private final Not not = new Not();
    private final AtSign at = new AtSign();
    private final Underscore underscore = new Underscore();
    private final Caret caret = new Caret();
    private final Ampersand amp = new Ampersand();
    private final Dot dot = new Dot();
    private final Eof eof = new Eof();
    private final This th = new This();
    private final Comma comma = new Comma();

    private final String text;
    private int current;
    private Token cached;
    public Tokenizer(String t) {
        this.text = t;
        this.current = 0;
    }
    public Token peek() {
        if (cached == null) {
            cached = advanceCurrent();
        }
        return cached;
    }
    public Token next() {
        if (cached == null) {
            return advanceCurrent();
        } else {
            Token tmp = cached;
            cached = null;
            return tmp;
        }
    }
    private Token advanceCurrent() {
        while (current < text.length() && Character.isWhitespace(text.charAt(current))) {
            current++;
        }
        if (current >= text.length()) {
            return this.eof;
        }
        switch (text.charAt(current)) {
            case '(': current++; return lp;
            case ')': current++; return rp;
            case '{': current++; return lb;
            case '}': current++; return rb;
            case '[': current++; return lbk;
            case ']': current++; return rbk;
            case ':': current++; return colon;
            case '!': current++; return not;
            case '@': current++; return at;
            case '^': current++; return caret;
            case '&': current++; return amp;
            case '.': current++; return dot;
            case ',': current++; return comma;
            case '_': current++; return underscore;

            case '<': 
                current++; 
                if (text.charAt(current) != '=')
                    return new Operator("<");
                current++;
                return new Operator("<=");
            case '>':
                current++; 
                if (text.charAt(current) != '=')
                    return new Operator(">");
                current++;
                return new Operator(">=");
            case '+': current++; return new Operator("+");
            case '-': current++; return new Operator("-");
            case '*': current++; return new Operator("*");
            case '/': current++; return new Operator("/");
            case '=': 
                current++; 
                if (text.charAt(current) != '=')
                    return new Operator("=");
                current++;
                return new Operator("==");
            
            default:
                if (Character.isDigit(text.charAt(current))) {
                    // This is a digit
                    int start = current++;
                    while (current < text.length() && Character.isDigit(text.charAt(current))) current ++;
                    // current now points to the first non-digit character, or past the end of the text
                    return new Number(Integer.parseInt(text.substring(start,current)));
                }
                // Now down to keywords and identifiers
                else if (Character.isLetter(text.charAt(current))) {
                    int start = current++;
                    while (current < text.length() && Character.isLetterOrDigit(text.charAt(current))) current ++;
                    // current now points to the first non-alphanumeric character, or past the end of the string
                    String fragment = text.substring(start,current);
                    // Unlike the constant parsing switch above, this has already advanced current
                    switch (fragment) {
                        case "if": return iff;
                        case "ifonly": return ifonly;
                        case "else": return elseTok;
                        case "while": return w;
                        case "return": return ret;
                        case "method": return method;
                        case "class": return tClass;
                        case "print": return print;
                        case "this": return th;
                        case "with": return with;
                        case "fields": return fields;
                        case "main": return main;
                        case "locals": return locals;
                        default: return new Identifier(fragment);
                    }
                } else {
                    throw new IllegalArgumentException("Unsupported character: "+text.charAt(current));
                }
        }
    }
}

//Expressions
sealed interface Expression 
    permits Constant, Binop, MethodCall, FieldRead, ClassRef, ThisExpr, Variable {
}
record ThisExpr() implements Expression {}
record Constant(long value) implements Expression {}
record Binop(Expression lhs, String op, Expression rhs) implements Expression {}
record MethodCall(Expression base, String methodname, List<Expression> args) implements Expression {}
record FieldRead(Expression base, String fieldname) implements Expression {}
record ClassRef(String classname) implements Expression {}
record Variable(String name) implements Expression {}


enum StatementType {
    ASSIGN,
    VOID,
    FIELDWRITE,
    IFELSE,
    IFONLY,
    WHILE,
    RETURN,
    PRINT
}
//Statements
sealed interface Statement
    permits AssignStmt, VoidStmt, FieldWriteStmt, IfElseStmt, IfOnlyStmt, WhileStmt, ReturnStmt, PrintStmt {
        public StatementType getType();
}
record AssignStmt(Variable var, Expression rhs) implements Statement{ @Override public StatementType getType(){ return StatementType.ASSIGN; } }
record VoidStmt(Expression rhs) implements Statement{ @Override public StatementType getType(){ return StatementType.VOID; } }
record FieldWriteStmt(Expression base, String fieldname, Expression rhs) implements Statement{ @Override public StatementType getType(){ return StatementType.FIELDWRITE; } }
record IfElseStmt(Expression cond, ArrayList<Statement> body, ArrayList<Statement> elseBody) implements Statement{ @Override public StatementType getType(){ return StatementType.IFELSE; } }
record IfOnlyStmt(Expression cond, ArrayList<Statement> body) implements Statement{ @Override public StatementType getType(){ return StatementType.IFONLY; } }
record WhileStmt(Expression cond, ArrayList<Statement> body) implements Statement{ @Override public StatementType getType(){ return StatementType.WHILE; } }
record ReturnStmt(Expression output) implements Statement{ @Override public StatementType getType(){ return StatementType.RETURN; } }
record PrintStmt(Expression str) implements Statement{ @Override public StatementType getType(){ return StatementType.PRINT; } }

//Method & Class
record Method(String name, ArrayList<String> args, ArrayList<String> locals, ArrayList<Statement> body){ @Override public boolean equals(Object o) {return this.name.equals(((Method)o).name());} }
record Class(String name, ArrayList<String> fields, ArrayList<Method> methods){ @Override public boolean equals(Object o) { return this.name.equals(((String)o)); } }

class ParsedCode {
    public final Method main;
    public final ArrayList<Class> classes;

    public ParsedCode(Method main, ArrayList<Class> classes) {
        this.main = main;
        this.classes = classes;
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("`````````````````````\n\tPARSED CODE\n");
        for (Class c : classes) {
            sb.append("CLASS\n");
            sb.append(c.toString() + "\n");
        }
        sb.append("MAIN\n");
        sb.append(main.toString());
        return sb.toString();
    }
}

class Parser {
    private Tokenizer tok;
    public Parser(Tokenizer t) {
        tok = t;
    }

    public Expression parseExpr() {
        switch (tok.next()) {
            case Eof eof: throw new IllegalArgumentException("No expression to parse: EOF");
            case Number n: return new Constant(n.value());
            case Identifier i: return new Variable(i.name());
            case LeftParen p:
                // Should be start of a binary operation
                Expression lhs = parseExpr();
                Token optok = tok.next();
                if (optok.getType() != TokenType.OPERATOR)
                    throw new IllegalArgumentException("Expected operator but found "+optok);
                Expression rhs = parseExpr();
                Token closetok = tok.next();
                if (closetok.getType() != TokenType.RIGHT_PAREN)
                    throw new IllegalArgumentException("Expected right paren but found "+closetok);
                return new Binop(lhs, ((Operator)optok).op(), rhs);
            case Ampersand a:
                // Should be field read
                Expression base = parseExpr();
                Token dot = tok.next();
                if (dot.getType() != TokenType.DOT)
                    throw new IllegalArgumentException("Expected dot but found "+dot);
                Token fname = tok.next();
                if (fname.getType() != TokenType.IDENTIFIER)
                    throw new IllegalArgumentException("Expected valid field name but found "+fname);
                return new FieldRead(base, ((Identifier)fname).name());
            case Caret c:
                // Should be method call
                Expression mbase = parseExpr();
                Token mdot = tok.next();
                if (mdot.getType() != TokenType.DOT)
                    throw new IllegalArgumentException("Expected dot but found "+mdot);
                Token mname = tok.next();
                if (mname.getType() != TokenType.IDENTIFIER)
                    throw new IllegalArgumentException("Expected valid method name but found "+mname);
                Token open = tok.next();
                if (open.getType() != TokenType.LEFT_PAREN)
                    throw new IllegalArgumentException("Expected left paren but found "+open);
                // Now we iterate through arguments
                ArrayList<Expression> args = new ArrayList<>();
                while (tok.peek().getType() != TokenType.RIGHT_PAREN) {
                    Expression e = parseExpr();
                    args.add(e);
                    // Now either a paren or a comma
                    Token punc = tok.peek();
                    if (punc.getType() == TokenType.COMMA)
                        tok.next(); // throw away the comma
                    else if(punc.getType() != TokenType.RIGHT_PAREN)
                        throw new IllegalArgumentException("Expected either ',' or ')', found "+punc);
                }
                tok.next(); //throw away right-paren
                return new MethodCall(mbase, ((Identifier)mname).name(), args);
            case AtSign a:
                Token cname = tok.next();
                if (cname.getType() != TokenType.IDENTIFIER)
                    throw new IllegalArgumentException("Expected valid class name but found: "+cname);
                return new ClassRef(((Identifier)cname).name());
            case This t: return new ThisExpr();
            case Token o:
                throw new IllegalArgumentException("Token "+o+" is not a valid start of an expression");
        }
    }

    public Statement parseStmt() {
        Token eql, colon, lbrace;
        Expression cond;
        ArrayList<Statement> ifBody;
        switch(tok.peek()) {
            case Underscore u: //void: _ = <expr>
                tok.next();
                eql = tok.next();
                    if(eql.getType() != TokenType.OPERATOR && ((Operator) eql).getOp().equals("="))
                        throw new IllegalArgumentException("Expected '=' but found "+eql);
                return new VoidStmt(parseExpr());
            case Not n: //Field write !expr.field = expr
                tok.next();
                Expression base = parseExpr();
                Token dot = tok.next();
                if (dot.getType() != TokenType.DOT)
                    throw new IllegalArgumentException("Expected dot but found " + dot);
                Token fname = tok.next();
                if (fname.getType() != TokenType.IDENTIFIER)
                    throw new IllegalArgumentException("Expected valid field name but found " + fname);
                eql = tok.next();
                if (eql.getType() != TokenType.OPERATOR && ((Operator) eql).getOp().equals("="))
                    throw new IllegalArgumentException("Expected '=' but found " + eql);
                Expression rhs = parseExpr();
                return new FieldWriteStmt(base, ((Identifier) fname).name(), rhs);
            case If i: //if e: { <newline> <one or more statements> } else { <newline> <one or more statements> }
                tok.next();
                cond = parseExpr();
                colon = tok.next();
                if (colon.getType() != TokenType.COLON)
                    throw new IllegalArgumentException("Expected ':' but found " + colon);
                lbrace = tok.next();
                if (lbrace.getType() != TokenType.LEFT_BRACE)
                    throw new IllegalArgumentException("Expected '{' but found " + lbrace);
                ifBody = new ArrayList<Statement>();
                while (tok.peek().getType() != TokenType.RIGHT_BRACE) {
                    ifBody.add(parseStmt());
                    if (tok.peek().getType() == TokenType.EOF)
                        throw new IllegalArgumentException("Reached EOF while parsing if statement");
                }
                tok.next(); // throw out right brace
                Token elseT = tok.next();
                if (elseT.getType() != TokenType.ELSE)
                    throw new IllegalArgumentException("Expected 'else' but found " + elseT);
                lbrace = tok.next();
                if (lbrace.getType() != TokenType.LEFT_BRACE)
                    throw new IllegalArgumentException("Expected '{' but found " + lbrace);
                ArrayList<Statement> elseBody = new ArrayList<Statement>();
                while (tok.peek().getType() != TokenType.RIGHT_BRACE) {
                    elseBody.add(parseStmt());
                    if (tok.peek().getType() == TokenType.EOF)
                        throw new IllegalArgumentException("Reached EOF while parsing else statement");
                }
                tok.next(); // throw out right brace
                return new IfElseStmt(cond, ifBody, elseBody);
            case IfOnly i: //ifonly e: { <newline> <one or more statements> }
                tok.next();
                cond = parseExpr();
                colon = tok.next();
                if(colon.getType() != TokenType.COLON)
                    throw new IllegalArgumentException("Expected ':' but found "+colon);
                lbrace = tok.next();
                if(lbrace.getType() != TokenType.LEFT_BRACE)
                    throw new IllegalArgumentException("Expected '{' but found "+lbrace);
                ifBody = new ArrayList<Statement>();
                while(tok.peek().getType() != TokenType.RIGHT_BRACE) {
                    ifBody.add(parseStmt());
                    if(tok.peek().getType() == TokenType.EOF)
                        throw new IllegalArgumentException("Reached EOF while parsing if statement");
                }        
                tok.next(); //throw out right brace
                return new IfOnlyStmt(cond, ifBody); 
            case While w: //while e: { <newline> <one or more statements> }
                tok.next();
                cond = parseExpr();
                colon = tok.next();
                if(colon.getType() != TokenType.COLON)
                    throw new IllegalArgumentException("Expected ':' but found "+colon);
                lbrace = tok.next();
                if(lbrace.getType() != TokenType.LEFT_BRACE)
                    throw new IllegalArgumentException("Expected '{' but found "+lbrace);
                ArrayList<Statement> body = new ArrayList<Statement>();
                while(tok.peek().getType() != TokenType.RIGHT_BRACE) {
                    body.add(parseStmt());
                    if(tok.peek().getType() == TokenType.EOF)
                        throw new IllegalArgumentException("Reached EOF while parsing if statement");
                }        
                tok.next(); //throw out right brace
                return new WhileStmt(cond, body);
            case Return r: //return: return(expr)
                tok.next();
                Expression out = parseExpr();
                return new ReturnStmt(out);
            case Print p:
                tok.next(); //print: print(expr)
                Token lparen = tok.next();
                if(lparen.getType() != TokenType.LEFT_PAREN)
                    throw new IllegalArgumentException("Expected '(' but found "+lparen);
                Expression prt = parseExpr();
                Token rparen = tok.next();
                if(rparen.getType() != TokenType.RIGHT_PAREN)
                    throw new IllegalArgumentException("Expected ')' but found "+rparen);
                return new PrintStmt(prt);
            default: //nothing else, check if its a start of an expression and see if its a variable assignment
                switch (parseExpr()) {
                    case Variable v: // assignment: v = <expr>
                        eql = tok.next();
                        if(eql.getType() != TokenType.OPERATOR || !((Operator) eql).getOp().equals("="))
                            throw new IllegalArgumentException("Expected '=' but found "+eql);
                        return new AssignStmt(v, parseExpr());
                    default:
                        throw new IllegalArgumentException("Could not find valid statement or expression");
                }
        }
    }

    public Method parseMethod() {
        Token method = tok.next();
        if(method.getType() != TokenType.METHOD)
            throw new IllegalArgumentException("Expected 'method', found"+method);
        Token nameTok = tok.next();
        if(nameTok.getType() != TokenType.IDENTIFIER)
            throw new IllegalArgumentException(nameTok+" is not a valid method name");
        String name = ((Identifier)nameTok).name();
        Token lparen = tok.next();
        if(lparen.getType() != TokenType.LEFT_PAREN)
            throw new IllegalArgumentException("Expected '(', found"+lparen);
        ArrayList<String> args = new ArrayList<>();
        while(tok.peek().getType() != TokenType.RIGHT_PAREN) {
            Token id = tok.next();
            if(id.getType() != TokenType.IDENTIFIER)
                throw new IllegalArgumentException("Error parsing arg "+(args.size()+1)+" of method "+name+": Expected variable identifier, found"+id);
            args.add(((Identifier)id).name());
            Token punc = tok.peek();
            if (punc.getType() == TokenType.COMMA)
                tok.next(); // throw away the comma
            else if(punc.getType() != TokenType.RIGHT_PAREN)
                throw new IllegalArgumentException("Expected either ',' or ')', found "+punc);
        }
        tok.next(); //throw out right paren
        Token with = tok.next();
        if(with.getType() != TokenType.WITH) 
            throw new IllegalArgumentException("Expected 'with', found"+with);
        Token tLocals = tok.next();
        if(tLocals.getType() != TokenType.LOCALS) 
            throw new IllegalArgumentException("Expected 'locals', found"+tLocals);
        ArrayList<String> locals = new ArrayList<>();
        while(tok.peek().getType() != TokenType.COLON) {
            Token id = tok.next();
            if(id.getType() != TokenType.IDENTIFIER)
                throw new IllegalArgumentException("Error parsing local "+(locals.size()+1)+" of method "+name+": Expected variable identifier, found"+id);
            locals.add(((Identifier)id).name());
            Token punc = tok.peek();
            if (punc.getType() == TokenType.COMMA)
                tok.next(); // throw away the comma
            else if(punc.getType() != TokenType.COLON)
                throw new IllegalArgumentException("Expected either ',' or ':', found "+punc);
        }
        tok.next(); //throw away colon
        ArrayList<Statement> body = new ArrayList<>();
        while(tok.peek().getType() != TokenType.METHOD && tok.peek().getType() != TokenType.EOF && tok.peek().getType() != TokenType.RIGHT_BRACK) {
            try {
                body.add(parseStmt());
                if(tok.peek().getType() == TokenType.RETURN) { //unconditional return
                    body.add(parseStmt());
                    break;
                }
            } catch(IllegalArgumentException e) {
                throw new IllegalArgumentException("Error parsing statement "+(body.size()+1)+" of method "+name+": "+e.getMessage());
            }
        }
        return new Method(name, args, locals, body);
    }

    public Method parseMain() {
        Token method = tok.next();
        if(method.getType() != TokenType.MAIN)
            throw new IllegalArgumentException("Expected 'main', found"+method);
        String name = "main";
        Token with = tok.next();
        if(with.getType() != TokenType.WITH) 
            throw new IllegalArgumentException("Expected 'with', found"+with);
        ArrayList<String> locals = new ArrayList<>();
        while(tok.peek().getType() != TokenType.COLON) {
            Token id = tok.next();
            if(id.getType() != TokenType.IDENTIFIER)
                throw new IllegalArgumentException("Expected variable identifier, found"+id);
            locals.add(((Identifier)id).name());
            Token punc = tok.peek();
            if (punc.getType() == TokenType.COMMA)
                tok.next(); // throw away the comma
            else if(punc.getType() != TokenType.COLON)
                throw new IllegalArgumentException("Expected either ',' or ':', found "+punc);
        }
        tok.next(); //throw away colon
        ArrayList<Statement> body = new ArrayList<>();
        while(tok.peek().getType() != TokenType.EOF) {
            body.add(parseStmt());
        }
        return new Method(name, new ArrayList<>(), locals, body);
    }

    public Class parseClass () {
        Token tClass = tok.next();
        if(tClass.getType() != TokenType.CLASS)
            throw new IllegalArgumentException("Expected top-level class declaration, found"+tClass);
        Token nameTok = tok.next();
        if(nameTok.getType() != TokenType.IDENTIFIER)
            throw new IllegalArgumentException(nameTok+" is not a valid class name");
        String name = ((Identifier)nameTok).name();
        Token lbrack = tok.next();
        if(lbrack.getType() != TokenType.LEFT_BRACK)
            throw new IllegalArgumentException("Expected '[', found"+lbrack);
        Token tFields = tok.next();
        if(tFields.getType() != TokenType.FIELDS) 
            throw new IllegalArgumentException("Expected 'fields', found"+tFields);
        ArrayList<String> fields = new ArrayList<>();
        while(tok.peek().getType() != TokenType.METHOD) {
            Token id = tok.next();
            if(id.getType() != TokenType.IDENTIFIER)
                throw new IllegalArgumentException("Expected variable identifier, found"+id);
            fields.add(((Identifier)id).name());
            Token punc = tok.peek();
            if (punc.getType() == TokenType.COMMA)
                tok.next(); // throw away the comma
            else if(punc.getType() != TokenType.METHOD)
                throw new IllegalArgumentException("Expected either ',' or 'method', found "+punc);
        }
        ArrayList<Method> methods = new ArrayList<>();
        while(tok.peek().getType() != TokenType.RIGHT_BRACK) {
            methods.add(parseMethod());
        } 
        tok.next(); //throw away right bracket
        return new Class(name, fields, methods);
    }

    public ParsedCode parse() { // parse EVERYTHING in the input as a series of Classes
        ArrayList<Class> classes = new ArrayList<>();
        while (tok.peek().getType() != TokenType.MAIN) {
            classes.add(parseClass());
        }
        Method main = parseMain();
        return new ParsedCode(main, classes);
    }
}

sealed interface CFGElement 
    permits CFGOp, CFGJumpOp, CFGMethod, CFGExpr {}

sealed interface CFGExpr extends CFGElement
    permits CFGValue, CFGArray, CFGGet, CFGPhi, CFGCall, CFGAlloc, CFGLoad, CFGBinOp {}

sealed interface CFGValue extends CFGExpr
    permits CFGVar, CFGPrimitive {}

sealed interface CFGOp extends CFGElement
    permits CFGAssn, CFGPrint, CFGSet, CFGStore {
        //public CFGOpType getType();
}

non-sealed class CFGPrint implements CFGOp {
    private CFGValue val;
    
    public CFGPrint(CFGValue val) {
        this.val = val;
    }

    public CFGValue val() {
        return val;
    }

    public void setVal(CFGValue val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return "print(" + val + ")";
    }
}

non-sealed class CFGAssn implements CFGOp {
    private CFGVar var;
    private CFGExpr expr;
    
    @Override
    public String toString() {
        return var + " = " + expr;
    }

    public CFGVar var() {
        return var;
    }

    public void setVar(CFGVar var) {
        this.var = var;
    }

    public CFGExpr expr() {
        return expr;
    }

    public void setExpr(CFGExpr expr) {
        this.expr = expr;
    }

    public CFGAssn(CFGVar var, CFGExpr expr) {
        this.var = var;
        this.expr = expr;
    }
}

record CFGBinOp(CFGExpr lhs, String op, CFGExpr rhs) implements CFGExpr {@Override public String toString() { return lhs + " " + op + " " + rhs; } }
record CFGGet(CFGVar arr, CFGValue val) implements CFGExpr { @Override public String toString() {return "getelt(" + arr + ", " + val + ")"; } }
record CFGSet(CFGVar addr, CFGExpr i, CFGExpr i2) implements CFGOp { @Override public String toString() {return "setelt("+addr+", "+i+", "+i2+")"; } }
record CFGAlloc(CFGPrimitive size) implements CFGExpr { @Override public String toString() { return "alloc("+size+")"; } }
record CFGCall(CFGVar addr, CFGVar receiver, CFGValue[] args) implements CFGExpr {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("call(");
        sb.append(addr + ", " + receiver);
        for(CFGValue a : args)
            sb.append(", ").append(a);
        return sb.append(')').toString();
    }
} 
record CFGLoad(CFGVar base) implements CFGExpr { @Override public String toString() { return "load(" + base + ")"; } }
record CFGStore(CFGVar base, CFGExpr i) implements CFGOp { @Override public String toString() { return "store(" +base+", "+i+ ")"; } }

sealed interface CFGJumpOp extends CFGElement
    permits CFGAutoJumpOp, CFGRetOp, CFGCondOp, CFGFail {

}
record CFGAutoJumpOp(BasicBlock target) implements CFGJumpOp { @Override public String toString() { return "jump " + target.getIdentifier(); } }
record CFGRetOp(CFGValue val) implements CFGJumpOp { @Override public String toString() { return "ret " + val; } }
record CFGCondOp(CFGValue test, BasicBlock yes, BasicBlock no) implements CFGJumpOp { @Override public String toString() { return "if "+test+" then "+yes.getIdentifier()+" else "+no.getIdentifier(); } }
record CFGFail(CFGFailOpt fail) implements CFGJumpOp { @Override public String toString() {return "fail "+fail.name(); } }
enum CFGFailOpt {
    NotANumber,
    NotAPointer,
    NoSuchField,
    NoSuchMethod
}


non-sealed class CFGVar implements CFGValue {
    private final String name;
    private final int version;

    public CFGVar(String name, int version) {
        this.name = name;
        this.version = version;
    }

    public String name() {
        return name;
    }

    public int version() {
        return version;
    }

    public CFGVar(CFGVar prev) {
        this(prev.name, prev.version + 1);
    }

    public CFGVar(String name) {
        this(name, -1);
    }

    public boolean isThis() {
        return name.equals("this");
    }

    @Override public boolean equals(Object o) {
        try {
            return name.equals(((CFGVar)o).name());
        } catch (Exception e) {
            return false; //comparing CFGVar and non-CFGVar
        }
    }


    @Override
    public final String toString() {
        if(name.equals("this"))
            return "%this";
        return "%"+this.name+(this.version>=0?this.version:"");
    }
}
record CFGPrimitive(long value) implements CFGValue { @Override public String toString() {return ""+this.value; } }

record CFGMethod(String name, CFGVar[] args, CFGVar[] locals, BasicBlock addr, ArrayList<BasicBlock> blocks) implements CFGElement {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(name.equals("main"))
            sb.append("main:\n"+addr);
        else {
            sb.append(name + "(");
            for (int i = 0; i < args.length; i++) {
                CFGVar v = args[i];
                sb.append(v);
                if (i < args.length - 1)
                    sb.append(", ");
            }
            sb.append("):\n" + addr);
        }
        for (int i = 1; i < blocks.size(); i++) {
            sb.append("\n"+blocks.get(i).getIdentifier()+":\n").append(blocks.get(i));
        }
        return sb.toString();
    }
}

record CFGArray(String name, Object[] elems) implements CFGExpr { int size()  {return this.elems.length; } @Override public String toString() { return "@"+name;} }
record CFGPhi(ArrayList<BasicBlock> blocks, ArrayList<CFGVar> varVersions) implements CFGExpr {
    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("phi(");
        for(int i = 0; i < blocks.size(); i++) {
            sb.append(blocks.get(i).getIdentifier()).append(", ");
            sb.append(varVersions.get(i)).append((i < blocks.size() - 1) ? ", " : "");
        }
        return sb.append(")").toString();
    }
}
record CFGClass(String name, CFGArray fields, CFGArray vtable, int numFields, ArrayList<CFGMethod> methods) {
    @Override public String toString() { 
        StringBuilder sb = new StringBuilder();
        for(CFGMethod m : methods) {
            sb.append(m).append('\n');
        }
        return sb.toString();
    }
}

class CtrlFlowGraph {
    public static ArrayList<BasicBlock> BasicBlocks;
    public static DataBlock CFGDataBlock;
    public static ArrayList<String> globals;
    public static ArrayList<String> methods;
    public static CFGMethod main;
    public static ArrayList<CFGClass> classes;
    public static ParsedCode parsedCode;

    public CtrlFlowGraph() {
        //empty constructor
    }

    public void mkCfg (ParsedCode code) {
        classes = new ArrayList<>();
        parsedCode = code;
        CFGDataBlock = new DataBlock(new ArrayList<>());
        //setup fields and vtables
        CFGArray vtable;
        CFGArray fields;
        globals = new ArrayList<>();
        methods = new ArrayList<>();
        ArrayList<String> uniqueFields = new ArrayList<>();
        ArrayList<Method> uniqueMethods = new ArrayList<>();
        for(Class c : code.classes) { // find all unique field & method names
            for(String f : c.fields()) {
                if(!uniqueFields.contains(f)) {
                    uniqueFields.add(f);
                    globals.add(f);
                }
            }
            for(Method m : c.methods()) {
                if(!uniqueMethods.contains(m)) 
                    uniqueMethods.add(m);
                    methods.add(m.name());
            }
            
        }
        
        for(Class c : code.classes) { // build fields and vtables
            vtable = new CFGArray("vtbl"+c.name(), new String[uniqueMethods.size()]);
            for(int i = 0; i < uniqueMethods.size(); i++) {
                if(c.methods().contains(uniqueMethods.get(i))) {
                    vtable.elems()[i] = uniqueMethods.get(i).name() + c.name();
                }
                else {
                    vtable.elems()[i] = "0";
                }
            }

            fields = new CFGArray("fields"+c.name(), new Integer[uniqueFields.size()]);
            int numFields =  1;
            for(int i = 0; i < uniqueFields.size(); i++) {
                if(c.fields().contains(uniqueFields.get(i))) {
                    fields.elems()[i] = 1 + numFields;
                    numFields++;
                }
                else {
                    fields.elems()[i] = 0;
                }
                
            }
            CFGDataBlock.data().add(vtable);
            CFGDataBlock.data().add(fields);
            classes.add(new CFGClass(c.name(), fields, vtable, c.fields().size(), new ArrayList<>()));
        }

        BasicBlocks = new ArrayList<>();
        for(int i = 0; i < code.classes.size(); i++) {
            Class c = code.classes.get(i);
            CFGClass cfgClass = classes.get(i);
            for(Method m : c.methods()) {
                cfgClass.methods().add(methodToCfg(m, c.name(), false));
            }
        }

        main = methodToCfg(code.main, "", true);

        //then process main method by recursively parsing called methods
    }

    public static CFGClass findClass(String s) {
        for(CFGClass c : classes) {
            if(c.name().equals(s))
                return c;
        }
        return null;
    }
    
    private CFGMethod methodToCfg(Method m, String classname, boolean isMain) {
        CFGVar tmp = new CFGVar("");
        ArrayList<CFGVar> activeVars = new ArrayList<>();
        CFGVar[] args = new CFGVar[0];
        if (!isMain) {
            args = new CFGVar[m.args().size()+1];
            args[0] = new CFGVar("this");
            for (int i = 1; i < args.length; i++) {
                args[i] = new CFGVar(m.args().get(i-1));
            }
            Collections.addAll(activeVars, args);
        }
        CFGVar[] locals = new CFGVar[0];
        locals = new CFGVar[m.locals().size()];
        for (int i = 0; i < locals.length; i++) {
            locals[i] = new CFGVar(m.locals().get(i));
        }
        BasicBlock.blockId = 0;
        
        ArrayList<BasicBlock> blocksInMethod = new ArrayList<>();
        BasicBlock start = new BasicBlock(blocksInMethod, m.name()+classname, m.body(), 0, activeVars, new ArrayList<>(), tmp, locals, null);
        return new CFGMethod(m.name()+classname, args, locals, start, blocksInMethod);
    }

    public static int getFieldId(String fieldName) {
        for(int i = 0; i < CtrlFlowGraph.globals.size(); i++) {
            if(globals.get(i).equals(fieldName))
                return i;
        }
        return -1;
    }

    public static int getMethodId(String methodName) {
        for(int i = 0; i < CtrlFlowGraph.methods.size(); i++) {
            if(methods.get(i).equals(methodName))
                return i;
        }
        return -1;
    }


    public static CFGVar getActive(ArrayList<CFGVar> actives, String varName) {
        for(CFGVar v : actives) {
            if(v.name().equals(varName)) {
                return v;
            }
        }
        return null;
    }

    public void toSSA() {
        ArrayList<CFGVar> varMap;
        for (CFGClass c : classes) {
            for(CFGMethod m : c.methods()) {
                varMap = new ArrayList<>();
                for(CFGVar a : m.args()) {
                    varMap.add(a);
                }
                for(CFGVar a : m.locals()) {
                    varMap.add(a);
                    //locals get one free "reassignment" before they start getting reassigned
                    //(they start undefined so they need to be defined once)
                }
                m.blocks().get(0).toSSA(varMap);
            }
        }

        varMap = new ArrayList<>();
        for (CFGVar a : main.locals()) {
            varMap.add(a);
            // locals get one free "reassignment" before they start getting reassigned
            // (they start undefined so they need to be defined once)
        }
        main.blocks().get(0).toSSA(varMap);
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(CFGDataBlock);
        sb.append("code:\n\n");
        for(CFGClass c : classes) {
            sb.append(c).append('\n');
        }
        sb.append(main);
        return sb.toString();
    }    
}

record DataBlock(ArrayList<CFGArray> data){
    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("data:\n");
        for(CFGArray arr : data) {
            sb.append("global array "+arr.name()+": { ");
            for(int i = 0; i < arr.size(); i++) {
                sb.append(arr.elems()[i].toString());
                if(i != arr.size() - 1) {
                    sb.append(",");
                }
                sb.append(" ");
            }
            sb.append("}\n");

        }


        return sb.toString();
    }

}

//identifier: name of the basic block
//actives: list of most recent variable versions active in the block
//ops: non-jumping operations, in order
//jump: jump, return, or conditional that ends the block
class BasicBlock {
    public static int blockId = 0;
    public static int ptrFails = 0;
    public static int numberFails = 0;
    public static int fieldFails = 0;
    public static int methodFails = 0;
    private static BasicBlock currBlock;

    private String identifier;
    private ArrayList<CFGVar> actives;
    private ArrayList<CFGOp> ops;
    private CFGJumpOp jmp;
    private ArrayList<BasicBlock> preds;
    private ArrayList<BasicBlock> succs;
    private static CFGVar tmp;
    private boolean inSSA; //boolean determining if block is already in SSA - used to avoid infinite loops

    public BasicBlock(ArrayList<BasicBlock> blocksInMethod, String blockBaseName, ArrayList <Statement> stmts, int startIndex, ArrayList<CFGVar> actives, 
        ArrayList<BasicBlock> preds, CFGVar tmp, CFGVar[] locals, BasicBlock jmpBack) {
            this(blocksInMethod, tmp, preds, actives);
            this.setupBlock(blocksInMethod, blockBaseName, stmts, startIndex, locals, jmpBack);
        }
    
    public BasicBlock(ArrayList<BasicBlock> blocksInMethod, CFGVar tmp) { //placeholder constructor to just initialize arraylists
        if(tmp != null)
            BasicBlock.tmp = tmp;
        inSSA = false;
        currBlock = this;
        blocksInMethod.add(this);
        CtrlFlowGraph.BasicBlocks.add(this);
        preds = new ArrayList<>();
        succs = new ArrayList<>();
        actives = new ArrayList<>();
        ops = new ArrayList<>();
        jmp = null;
        return;
    }

    // create empty basic block with predecessor & active var setup
    // used where basic blocks are built manually (while, method call, field r/w)
    public BasicBlock(ArrayList<BasicBlock> blocksInMethod, CFGVar tmp, ArrayList<BasicBlock> preds, ArrayList<CFGVar> actives) {
        this(blocksInMethod, tmp);
        setPredsActives(preds, actives);
    }

    //create a fail block
    public BasicBlock(ArrayList<BasicBlock> blocksInMethod, CFGVar tmp, CFGFailOpt failType, ArrayList<BasicBlock> preds) {
        this(blocksInMethod, tmp);
        for(BasicBlock p : this.preds) {
            p.succs.add(this);
        }
        jmp = new CFGFail(failType);
        identifier = failType.toString();
        switch (failType) {
            case CFGFailOpt.NotANumber:
                identifier = identifier+numberFails;
                numberFails++;
                break;
            case CFGFailOpt.NotAPointer:
                identifier = identifier+ptrFails;
                ptrFails++;
                break;
            case CFGFailOpt.NoSuchField:
                    identifier = identifier+fieldFails;
                    fieldFails++;
                    break;
            case CFGFailOpt.NoSuchMethod:
                identifier = identifier+methodFails;
                methodFails++;
                break;
        }
    }

    //do majority of work to actually set up block - despite being non-static, mostly operates on the static field currBlock
    private void setupBlock(ArrayList<BasicBlock> blocksInMethod, String blockBaseName, ArrayList <Statement> stmts, int startIndex, 
        CFGVar[] locals, BasicBlock jmpBack) {
        if(identifier == null || identifier.equals(""))
            setIdentifier(blockBaseName);
        ArrayList<BasicBlock> localPreds = new ArrayList<>();
        //make phis
        for(BasicBlock p : this.preds) {
            p.addSucc(this);
        }
        BasicBlock afterIf=null, ifBlk, branchEntryBlock;
        CFGValue cond;
        for(int i = startIndex; i < stmts.size(); i++) {
            Statement s = stmts.get(i);
            switch (s) {
                case AssignStmt a:
                    CFGVar assignment = null;
                    String name = a.var().name();
                    CFGVar base = getActive(name);
                    CFGExpr operand = exprToCFG(blocksInMethod, blockBaseName, a.rhs(), locals, false);
                    if(base != null) {
                        if(base.isThis())
                            throw new IllegalArgumentException("Error: illegal write to \"this\"");
                        assignment = base; //variable has already been initialized, we are reassigning it
                    } 
                    for(CFGVar v : locals) {
                        if(v.name().equals(name)) {
                            assignment = v; //variable is a local that has not been initalized yet, we need to initialize it
                            currBlock.actives.add(assignment);
                            break;
                        }
                    } 
                    if(assignment == null)
                        throw new IllegalArgumentException("Post-Parse error: Cannot initialize variable "+name+" as it was neither passed as an argument nor declared as a local.");
                    currBlock.addOp(new CFGAssn(assignment, operand));
                    break;
                case IfElseStmt ie:
                    cond = (CFGValue)exprToCFG(blocksInMethod, blockBaseName, ie.cond(), locals, true);
                    localPreds.add(currBlock);
                    branchEntryBlock = currBlock;
                    if(i < stmts.size()-1)
                        afterIf = new BasicBlock(blocksInMethod, null);
                    ifBlk = new BasicBlock(blocksInMethod, blockBaseName, ie.body(), 0, actives, localPreds, null, locals, afterIf);  
                    BasicBlock elseBlk = new BasicBlock(blocksInMethod, blockBaseName, ie.elseBody(), 0, actives, localPreds, tmp, locals, afterIf);
                    localPreds.remove(this);
                    localPreds.add(ifBlk);
                    localPreds.add(elseBlk);
                    if(afterIf != null){
                        currBlock = afterIf;
                        afterIf.setPredsActives(localPreds, actives);
                        afterIf.setupBlock(blocksInMethod, blockBaseName, stmts, i + 1, locals, jmpBack);
                    }
                    branchEntryBlock.jmp = new CFGCondOp(cond, ifBlk, elseBlk);
                    localPreds.clear();
                    return;
                case IfOnlyStmt io:
                    cond = (CFGValue)exprToCFG(blocksInMethod, blockBaseName, io.cond(), locals, true);
                    localPreds = new ArrayList<>();
                    localPreds.add(currBlock);
                    branchEntryBlock = currBlock;
                    afterIf = new BasicBlock(blocksInMethod, null);
                    ifBlk = new BasicBlock(blocksInMethod, blockBaseName, io.body(), 0, actives, localPreds, null, locals, afterIf);
                    localPreds.add(ifBlk);
                    currBlock = afterIf;
                    afterIf.setupBlock(blocksInMethod, blockBaseName, stmts, i+1, locals, jmpBack);
                    branchEntryBlock.jmp = new CFGCondOp(cond, ifBlk, afterIf);
                    localPreds.clear();
                    return;
                case WhileStmt w:
                    cond = (CFGValue)exprToCFG(blocksInMethod, blockBaseName, w.cond(), locals, true);
                    branchEntryBlock = currBlock;
                    BasicBlock loophead = new BasicBlock(blocksInMethod, null);
                    localPreds.add(loophead);
                    loophead.addActives(actives);
                    BasicBlock body = new BasicBlock(blocksInMethod, blockBaseName, w.body(), 0, actives, localPreds, null, locals, loophead);
                    BasicBlock end = new BasicBlock(blocksInMethod, blockBaseName, w.body(), i + 1, actives, localPreds, null, locals, jmpBack);
                    localPreds.remove(loophead);
                    
                    localPreds.add(branchEntryBlock);
                    localPreds.add(body);
                    loophead.setupBlock(blocksInMethod, blockBaseName, new ArrayList<>(), 0, locals, null); //set up block with null body to build phi
                    loophead.addJump(new CFGCondOp(cond, body, end)); //add jump at end
                    branchEntryBlock.jmp = new CFGAutoJumpOp(loophead);  
                    return;
                case PrintStmt p:
                    CFGValue prt = (CFGValue)exprToCFG(blocksInMethod, blockBaseName, p.str(), locals, true);
                    currBlock.addOp(new CFGPrint(prt));
                    break;
                case FieldWriteStmt f: // can break if writing ptr to field
                    CFGValue objToStore = (CFGValue)currBlock.exprToCFG(blocksInMethod, blockBaseName, f.rhs(), locals, true); //evaluate rhs first
                    //can safely cast obj since it is known to be a var identifier by tokenizer
                    CFGVar obj = (CFGVar)exprToCFG(blocksInMethod, blockBaseName, f.base(), locals, true);
                    int fieldId = CtrlFlowGraph.getFieldId(f.fieldname()); //get index of field in fields arr
                    if(fieldId == -1)
                        throw new IllegalArgumentException("Attempt to modify never-declared field "+f.fieldname());
                    tmp = new CFGVar(tmp);
                    CFGVar objAddr = tmp;
                    currBlock.addOp(new CFGAssn(tmp, new CFGBinOp(obj, "&", new CFGPrimitive(1)))); //get fields arr
                    localPreds.add(currBlock);
                    BasicBlock beforeBranch = currBlock;
                    BasicBlock ptrFail = new BasicBlock(blocksInMethod, null,  CFGFailOpt.NotAPointer, localPreds); //basic block for fields ptr isnt real
                    BasicBlock getField = new BasicBlock(blocksInMethod, null, localPreds, actives); //basic block to attempt storing a var
                    beforeBranch.jmp = new CFGCondOp(objAddr, ptrFail, getField);
                    getField.setIdentifier(blockBaseName);
                    tmp = new CFGVar(tmp);
                    CFGVar fieldsAddr = tmp;
                    tmp = new CFGVar(tmp);
                    CFGVar fields = tmp;
                    getField.addOp(new CFGAssn(fieldsAddr, new CFGBinOp(obj, "+", new CFGPrimitive(8))));
                    getField.addOp(new CFGAssn(fields, new CFGLoad(fieldsAddr)));
                    tmp = new CFGVar(tmp);
                    CFGVar field = tmp;
                    getField.addOp(new CFGAssn(field, new CFGGet(fields, new CFGPrimitive(fieldId))));
                    localPreds.remove(currBlock);
                    localPreds.add(getField);
                    BasicBlock fieldFail = new BasicBlock(blocksInMethod, null, CFGFailOpt.NoSuchField, localPreds);
                    BasicBlock storeVar = new BasicBlock(blocksInMethod, null, localPreds, actives);
                    getField.jmp = new CFGCondOp(field, storeVar, fieldFail); //test for no such field
                    currBlock.addOp(new CFGSet(obj, field, objToStore));
                    storeVar.setupBlock(blocksInMethod, blockBaseName, stmts, i + 1, locals, jmpBack);
                    return; //rest of method is processed by setupBlock
                case ReturnStmt r:
                    CFGValue valToReturn = (CFGValue)exprToCFG(blocksInMethod, blockBaseName, r.output(), locals, true);
                    currBlock.jmp = new CFGRetOp(valToReturn);
                    break;
                case VoidStmt v:
                    CFGValue voidRslt = (CFGValue)exprToCFG(blocksInMethod, blockBaseName, v.rhs(), locals, true);
                    currBlock.addOp(new CFGAssn(new CFGVar(tmp), voidRslt));
                    break;
                    default:
                    break;
            }
            if(jmp != null && !(i == stmts.size()-1)) { //basic block has been finished in a submethod
                currBlock.setupBlock(blocksInMethod, blockBaseName, stmts, i + 1, locals, jmpBack);
                return;
            }
        }
        if (currBlock.jmp == null) {
            if (jmpBack == null)
                currBlock.jmp = new CFGRetOp(new CFGPrimitive(0));
            else
                currBlock.jmp = new CFGAutoJumpOp(jmpBack);
        }
        return;
    }

    //convert block to SSA form
    public void toSSA(ArrayList<CFGVar> varMap) {
        //initial make phis
        if(preds.size() >= 2) {
            ArrayList<BasicBlock> phiBlocks;
            ArrayList<CFGVar> phiVars;
            for(CFGVar v : actives) {
                if(v.equals("this") || v.equals(""))
                    continue;
                phiBlocks = new ArrayList<>();
                phiVars = new ArrayList<>();
                for(BasicBlock p : preds) {
                    CFGVar pVar = p.getActive(v.name());
                    phiBlocks.add(p);
                    phiVars.add(pVar);
                }
                CFGPhi phiOp = new CFGPhi(phiBlocks, phiVars);
                ops.add(0, new CFGAssn(v, phiOp));
            }
        }
        if(inSSA)
            return;
        inSSA = true;
        //phis get added up here
        for(CFGOp o : ops) {
            switch (o) {
                case CFGAssn a:
                    a.setExpr(exprToSSA(a.expr(), varMap));
                    //do whatever thing needs to be added for exprs
                    CFGVar base = a.var();
                    int index = varMap.indexOf(base);
                    if(index == -1) //assignment to temporary value
                        continue;
                    CFGVar storedVar = varMap.get(index);
                    CFGVar newVar = new CFGVar(storedVar);
                    a.setVar(newVar);
                    varMap.remove(storedVar);
                    varMap.add(newVar);
                    actives.remove(base);
                    actives.add(newVar);
                    break;
                case CFGPrint p:
                    p.setVal((CFGValue)exprToSSA(p.val(), varMap));
                    break;
                case CFGSet s:
                    break;
                case CFGStore s:
                    break;
                default:
                    break;
            }
        }
        for(BasicBlock succ : succs) {
            succ.toSSA(varMap);
        }
    }

    CFGExpr exprToSSA(CFGExpr expr, ArrayList<CFGVar> varMap) {
        switch(expr) {
            case CFGVar v:
                if(v.name().equals("") || v.name().equals("this"))
                    return expr;
                CFGVar currVer = varMap.get(varMap.indexOf(v));
                return currVer;
            case CFGPrimitive c:
                return expr;
            case CFGBinOp b:
                CFGExpr left = exprToSSA(b.lhs(), varMap);
                CFGExpr right = exprToSSA(b.rhs(), varMap);
                CFGExpr currExpr = new CFGBinOp(left, b.op(), right);
                return currExpr;
            case CFGCall c:
                CFGVar newAddr = (CFGVar)exprToSSA(c.addr(), varMap);
                CFGVar newReceiver = (CFGVar)exprToSSA(c.receiver(), varMap);
                CFGValue[] newArgs = new CFGVar[c.args().length];
                for(int i = 0; i < newArgs.length; i++) {
                    CFGValue v = c.args()[i];
                    newArgs[i] = (CFGValue)exprToSSA(v, varMap);
                }
                return new CFGCall(newAddr, newReceiver, newArgs);
            case CFGGet g:
                CFGVar arr = (CFGVar)exprToSSA(g.arr(), varMap);
                CFGValue val = (CFGValue)exprToSSA(g.val(), varMap);
                return new CFGGet(arr, val);
            case CFGLoad l:
                CFGVar base = (CFGVar)exprToSSA(l.base(), varMap);
                return new CFGLoad(base);
            default:
                    return expr;
        }
    }

    //set identifier (name) of a block
    private void setIdentifier(String blockBaseName) {
        this.identifier = blockBaseName + (blockId > 0 ? blockId : "");
        blockId++;
    }

    //sets predecessors and active variable lists of a block
    private void setPredsActives(ArrayList<BasicBlock> preds, ArrayList<CFGVar> actives) {
        this.preds.addAll(preds);
        if(preds.size() == 0) //if this is the starting pt of the method
            this.actives.addAll(actives);
        else
            this.actives.addAll(preds.get(0).getActives());
        for(BasicBlock p : this.preds) {
            p.succs.add(this);
        }
    }

    //print block as a String
    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        for(CFGOp op : ops) {
            sb.append("\t" + op + "\n");
        }
        sb.append("\t" + jmp);
        return sb.toString();
    }

    public ArrayList<CFGVar> getActives() {
        return actives;
    }

    public void addSucc(BasicBlock b) {
        succs.add(b);
    }

    //overwrite existing actives and replace it with v - intended to be use to temporarily pre-initialize in cases where loops are being turned into CFG
    public void addActives(ArrayList<CFGVar> v) {
        actives = v;
    }

    private void addOp(CFGOp c) {
        ops.add(c);
    }
    
    private void addJump(CFGJumpOp j) {
        jmp = j;
    }

    public String getIdentifier() {
        return identifier;
    }

    //convert a potentially complex CFG expr into a series of statements
    public CFGExpr exprToCFG(ArrayList<BasicBlock> blocksInMethod, String blockBaseName, Expression expr, CFGVar[] locals, boolean requireVal) {
        ArrayList<BasicBlock> localPreds = new ArrayList<>();
        BasicBlock badPtrBlock, badFieldBlock, badMethodBlock, getObjBlock;
        CFGExpr out;
        switch (expr) {
            case Constant c:
                return new CFGPrimitive(c.value());
            case Variable v:
                CFGVar tmpVar = getActive(v.name());
                if(tmpVar == null)
                    throw new IllegalArgumentException("Attempted to access nonexistent or uninitialized variable "+v.name() + " (expr "+expr+")\n\nCURR CFG:\n\n"+App.cfg);
                return tmpVar;
            case Binop b:
                CFGExpr lhs, rhs;
                lhs = exprToCFG(blocksInMethod, blockBaseName, b.lhs(), locals, true); 
                rhs = exprToCFG(blocksInMethod, blockBaseName, b.rhs(), locals, true);
                if(lhs instanceof CFGBinOp) {
                    tmp = new CFGVar(tmp);
                    currBlock.addOp(new CFGAssn(tmp, lhs));
                    lhs = tmp;
                }
                if(rhs instanceof CFGBinOp) {
                    tmp = new CFGVar(tmp);
                    currBlock.addOp(new CFGAssn(tmp, rhs));
                    rhs = tmp;
                }
                out = new CFGBinOp(lhs, b.op(), rhs);
                break;
            case ClassRef c: //used for class reference in a complex expression, so we need to return an anonymous(temp) value
                CFGClass classData = CtrlFlowGraph.findClass(c.classname());
                if(classData == null)
                    throw new IllegalArgumentException("Class "+c.classname()+" is undefined");
                tmp = new CFGVar(tmp);
                CFGVar cRef = tmp;
                actives.add(tmp);
                currBlock.addOp(new CFGAssn(tmp, new CFGAlloc(new CFGPrimitive(classData.numFields()+2)))); //alloc vtable, field map, fields
                currBlock.addOp(new CFGStore(tmp, classData.vtable()));
                tmp = new CFGVar(tmp);
                currBlock.addOp(new CFGAssn(tmp, new CFGBinOp(cRef, "+", new CFGPrimitive(8))));
                currBlock.addOp(new CFGStore(tmp, classData.fields()));
                out = cRef;
                break;
            case FieldRead f:
                int expectedFieldId = CtrlFlowGraph.getFieldId(f.fieldname());
                if(expectedFieldId == -1)
                    throw new IllegalArgumentException("Code attempts to read from never-defined field "+f.fieldname());
                tmp = new CFGVar(tmp);
                CFGVar objField = tmp;
                CFGVar obj = (CFGVar)exprToCFG(blocksInMethod, blockBaseName, f.base(), locals, true);
                getObjBlock = currBlock;
                currBlock.addOp(new CFGAssn(objField, new CFGBinOp(obj, "&", new CFGPrimitive(1))));
                localPreds.add(this);
                badPtrBlock = new BasicBlock(blocksInMethod, null, CFGFailOpt.NotAPointer, localPreds);
                BasicBlock getFieldId = new BasicBlock(blocksInMethod, null, localPreds, actives);
                getFieldId.setIdentifier(blockBaseName);
                getObjBlock.jmp = new CFGCondOp(objField, badPtrBlock, getFieldId);
                tmp = new CFGVar(tmp);
                CFGVar fieldAddr = tmp;
                getFieldId.addOp(new CFGAssn(fieldAddr, new CFGBinOp(obj, "+", new CFGPrimitive(8))));
                tmp = new CFGVar(tmp);
                CFGVar fieldMap = tmp;
                getFieldId.addOp(new CFGAssn(fieldMap, new CFGLoad(fieldAddr)));
                tmp = new CFGVar(tmp);
                CFGVar fieldOffset = tmp; //used to get the offset of the field
                getFieldId.addOp(new CFGAssn(fieldOffset, new CFGGet(fieldMap, new CFGPrimitive(expectedFieldId))));
                //check if field actually exists
                localPreds.remove(this);
                localPreds.add(getFieldId);
                badFieldBlock = new BasicBlock(blocksInMethod, null, CFGFailOpt.NoSuchField, localPreds);
                BasicBlock getField = new BasicBlock(blocksInMethod, null, localPreds, actives);
                getField.setIdentifier(blockBaseName);
                getFieldId.addJump(new CFGCondOp(fieldOffset, getField, badFieldBlock));
                tmp = new CFGVar(tmp);
                CFGVar field = tmp;
                getField.addOp(new CFGAssn(field, new CFGGet(obj, fieldOffset)));
                out = field;
                break;
            case MethodCall m:
                int methodId = CtrlFlowGraph.getMethodId(m.methodname());
                if(methodId == -1)
                    throw new IllegalArgumentException("Attempt to call nonexistent method"+m.methodname());
                obj = (CFGVar)exprToCFG(blocksInMethod, blockBaseName, m.base(), locals, true);
                tmp = new CFGVar(tmp);
                CFGVar objPtr = tmp;
                currBlock.addOp(new CFGAssn(objPtr, new CFGBinOp(obj, "&", new CFGPrimitive(1))));
                getObjBlock = currBlock;
                localPreds.add(this);
                badPtrBlock = new BasicBlock(blocksInMethod, null, CFGFailOpt.NotAPointer, localPreds);
                BasicBlock getMethodId = new BasicBlock(blocksInMethod, null, localPreds, actives);
                localPreds.clear();
                getObjBlock.jmp = new CFGCondOp(objPtr, badPtrBlock, getMethodId);
                getMethodId.setIdentifier(blockBaseName);
                //load vtable, find method
                tmp = new CFGVar(tmp);
                CFGVar vtbl = tmp;
                getMethodId.addOp(new CFGAssn(vtbl, new CFGLoad(obj)));
                tmp = new CFGVar(tmp);
                CFGVar methodAddr = tmp;
                getMethodId.addOp(new CFGAssn(methodAddr, new CFGGet(vtbl, new CFGPrimitive(methodId)))); //get vtable id
                localPreds.add(getMethodId);
                badMethodBlock = new BasicBlock(blocksInMethod, null, CFGFailOpt.NoSuchMethod, localPreds);
                BasicBlock callBlock = new BasicBlock(blocksInMethod, null, localPreds, actives);
                callBlock.setIdentifier(blockBaseName);
                getMethodId.jmp = new CFGCondOp(methodAddr, callBlock, badMethodBlock);
                localPreds.clear();
                tmp = new CFGVar(tmp);
                CFGVar callRslt = tmp;
                CFGValue[] args = new CFGValue[m.args().size()];
                for(int i = 0; i < args.length; i++) {
                    Expression e = m.args().get(i);
                    args[i] = (CFGValue)exprToCFG(blocksInMethod, blockBaseName, e, locals, true);
                }

                callBlock.addOp(new CFGAssn(callRslt, new CFGCall(methodAddr, obj, args))); //figure out receiver
                out = callRslt;
                break;
            case ThisExpr t:
                return getActive("this");
            default:
                return null;
        }
        if(requireVal) {
            tmp = new CFGVar(tmp);
            currBlock.addOp(new CFGAssn(tmp, out));
            out = tmp;
        }
        return out;
    }

    //determine if a name corresponds with an active variable
    //returns the variable if one exists and null otherwise
    public CFGVar getActive(String varName) {
        for(CFGVar v : actives) {
            if(v.name().equals(varName)) {
                return v;
            }
        }
        return null;
    }
}



public class App {
    public static CtrlFlowGraph cfg;
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: <comp> infile [-o outfile] [args...]");
            System.exit(1);
        }
        String inFilePath = args[0];
        String outFilePath = "";
        boolean ssa = true, outName = false;
        if(args.length >= 3 && args[1].equals("-o")) {
            
        }

        for(int nextArg =  1; nextArg < args.length; nextArg++) {
            switch (args[nextArg]) {
                case "-noSSA":
                    ssa = false;
                    break;
                case "-o":
                    if(nextArg >= (args.length - 1))
                        throw new IllegalArgumentException("Error: received -o flag but no following arg to designate output file");
                    if(outName)
                        throw new IllegalArgumentException("Error: attempted to specify illegal second output file");
                    nextArg++;
                    outFilePath = args[nextArg];
                    outName = true;
                    break;
                default:
                    System.out.print("Command-line arg "+args[nextArg]+" not recognized");
                    System.exit(1);
            }
        }
        
        String code = "";
        try {
            code = Files.readString(Path.of(inFilePath), StandardCharsets.UTF_8);
        } catch(Exception e) {
            System.err.println("Failed to locate file "+inFilePath);
            System.exit(1);
        }
        Tokenizer tok = new Tokenizer(code);
        Parser p = new Parser(tok);
        
        ParsedCode pc = p.parse();
        cfg = new CtrlFlowGraph();
        cfg.mkCfg(pc);
        if(ssa)
            cfg.toSSA();
        if(outFilePath == "") {
            System.out.println(cfg);
            return;
        }
        try {
            Files.write(Path.of(outFilePath), cfg.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
        } catch(Exception e) {
            System.err.println("Cannot write code to file "+outFilePath);
        }

    }
}
