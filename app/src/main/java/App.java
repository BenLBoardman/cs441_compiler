import java.lang.StringBuilder;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
            case '!':
                current ++; if(text.charAt(current) == '=') { current++; return new Operator("!="); } 
                return not;
            case '@': current++; return at;
            case '^': current++; return caret;
            case '&': current++; return amp;
            case '.': current++; return dot;
            case ',': current++; return comma;
            case '_': current++; return underscore;

            case '<':
                current++; 
                if (text.charAt(current) == '=') { current++; return new Operator("<="); }
                else if (text.charAt(current) == '<') { current++; return new Operator("<<"); }
                return new Operator("<");
            case '>':
                current++; 
                if (text.charAt(current) == '=') { current++; return new Operator(">="); }
                else if (text.charAt(current) == '>') { current++; return new Operator(">>"); }
                return new Operator(">");
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
record Binop(Expression lhs, String op, Expression rhs) implements Expression { public boolean isBool() {return op.equals("==") || op.equals("!=") || op.equals(">") || op.equals("<") || op.equals("<=") || op.equals(">="); }}
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
                if (optok.getType() != TokenType.OPERATOR || ((Operator)optok).getOp().equals("="))
                    throw new IllegalArgumentException("Expected non-assignment operator but found "+optok);
                Expression rhs = parseExpr();

                Token closetok = tok.next();
                if (closetok.getType() != TokenType.RIGHT_PAREN)
                    throw new IllegalArgumentException("Expected right paren but found "+closetok);
                Binop op = new Binop(lhs, ((Operator)optok).op(), rhs);
                if(!op.isBool() && (lhs instanceof ThisExpr || rhs instanceof ThisExpr)) {
                    throw new IllegalArgumentException("Error: Math operations cannot be performed on \"this\"");
                }
                return op;
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
        while(tok.peek().getType() != TokenType.METHOD
            && tok.peek().getType() != TokenType.EOF && tok.peek().getType() != TokenType.RIGHT_BRACK) {
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

sealed interface CFGOp extends CFGElement
    permits CFGAssn, CFGPrint, CFGSet, CFGStore {
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

non-sealed class CFGStore implements CFGOp {
    private CFGVar base;
    private CFGData index;

    public CFGVar base() {
        return base;
    }

    public void setBase(CFGVar base) {
        this.base = base;
    }

    public CFGExpr index() {
        return index;
    }

    public void setIndex(CFGData i) {
        this.index = i;
    }

    public CFGStore(CFGVar base, CFGData i) {
        this.base = base;
        this.index = i;
    }

    @Override
    public String toString() {
        return "store(" + base + ", " + index + ")";
    }
}

non-sealed class CFGSet implements CFGOp{
    private CFGVar addr;
    private CFGValue index;
    private CFGData val;

    public CFGVar addr() {
        return addr;
    }

    public void setAddr(CFGVar addr) {
        this.addr = addr;
    }

    public CFGValue index() {
        return index;
    }

    public void setIndex(CFGValue index) {
        this.index = index;
    }

    public CFGData val() {
        return val;
    }

    public void setVal(CFGData val) {
        this.val = val;
    }

    public CFGSet(CFGVar addr, CFGValue index, CFGData val) {
        this.addr = addr;
        this.index = index;
        this.val = val;
    }



    @Override
    public String toString() {
        return "setelt(" + addr + ", " + index + ", " + val + ")";
    }
}

sealed interface CFGExpr extends CFGElement
    permits CFGData, CFGGet, CFGPhi, CFGCall, CFGAlloc, CFGLoad, CFGBinOp { public boolean equals(Object o); }

record CFGBinOp(CFGValue lhs, String op, CFGValue rhs) implements CFGExpr {
    @Override
    public String toString() {
        return lhs + " " + op + " " + rhs;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof CFGBinOp))
            return false;
        CFGBinOp b = (CFGBinOp)o;
        return this.lhs == b.lhs && this.op == b.op && this.rhs == b.rhs;
    }

    public CFGExpr evalBinOp() {
        if (lhs instanceof CFGPrimitive && rhs instanceof CFGPrimitive) { // optimize out double-constant binops
            CFGPrimitive lprim = (CFGPrimitive) lhs;
            CFGPrimitive rprim = (CFGPrimitive) rhs;
            long rslt;
            switch (op) {
                case "+":
                    rslt = lprim.value() + rprim.value();
                    break;
                case "-":
                    rslt = lprim.value() - rprim.value();
                    break;
                case "*":
                    rslt = lprim.value() * rprim.value();
                    break;
                case "/":
                    rslt = lprim.value() / rprim.value();
                    break;
                case ">":
                    rslt = lprim.value() > rprim.value() ? 1 : 0;
                    break;
                case "<":
                    rslt = lprim.value() < rprim.value() ? 1 : 0;
                    break;
                case "<<":
                    rslt = lprim.value() << rprim.value();
                    break;
                case ">>":
                    rslt = lprim.value() >> rprim.value();
                    break;
                case "<=":
                    rslt = lprim.value() <= rprim.value() ? 1 : 0;
                    break;
                case ">=":
                    rslt = lprim.value() >= rprim.value() ? 1 : 0;
                    break;
                case "==":
                    rslt = lprim.value() == rprim.value() ? 1 : 0;
                    break;
                case "!=":rslt = lprim.value() != rprim.value() ? 1 : 0;
                    break;
                case "&": rslt = lprim.value() & rprim.value(); break;
                default: // should be unreachable
                    rslt = 0;
            }
            return CFGPrimitive.getPrimitive(rslt);
        }
        return this;
    }
}

record CFGGet(CFGVar arr, CFGValue val) implements CFGExpr {
    @Override
    public String toString() {
        return "getelt(" + arr + ", " + val + ")";
    }

    @Override public boolean equals(Object o) {
        if(!(o instanceof CFGGet))
            return false;
        CFGGet g = (CFGGet)o;
        return g.arr.equals(this.arr) && g.val.equals(this.val);
    }
}

record CFGAlloc(CFGPrimitive size) implements CFGExpr {
    @Override
    public String toString() {
        return "alloc(" + size + ")";
    }
}

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

record CFGLoad(CFGVar base) implements CFGExpr {
    @Override
    public String toString() {
        return "load(" + base + ")";
    }
    
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof CFGLoad))
            return false;
        CFGLoad l = (CFGLoad)o;
        return l.base.equals(this.base);
    } 
}

record CFGPhi(ArrayList<BasicBlock> blocks, ArrayList<CFGValue> varVersions) implements CFGExpr {
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

sealed interface CFGData extends CFGExpr
    permits CFGValue, CFGArray {}

sealed interface CFGJumpOp extends CFGElement
    permits CFGAutoJumpOp, CFGRetOp, CFGCondOp, CFGFail {

}
record CFGAutoJumpOp(BasicBlock target) implements CFGJumpOp { @Override public String toString() { return "jump " + target.getIdentifier(); } }

non-sealed class CFGRetOp implements CFGJumpOp {
    private CFGValue val;

    public CFGRetOp(CFGValue val) {
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
        return "ret " + val;
    }
}

non-sealed class CFGCondOp implements CFGJumpOp
{
    private CFGValue cond;
    private BasicBlock yes;
    private BasicBlock no;

    public CFGCondOp(CFGValue cond, BasicBlock yes, BasicBlock no) {
        this.cond = cond;
        this.yes = yes;
        this.no = no;
    }

    public CFGValue cond() {
        return cond;
    }
    public BasicBlock yes() {
        return yes;
    }
    public BasicBlock no() {
        return no;
    }

    public void setCond(CFGValue cond) {
        this.cond = cond;
    }

    @Override
    public String toString() {
        return "if " + cond + " then " + yes.getIdentifier() + " else " + no.getIdentifier();
    }
}

record CFGFail(CFGFailOpt fail) implements CFGJumpOp { @Override public String toString() {return "fail "+fail.name(); } }
enum CFGFailOpt {
    NotANumber,
    NotAPointer,
    NoSuchField,
    NoSuchMethod
}

sealed interface CFGValue extends CFGData
    permits CFGVar, CFGPrimitive {}
non-sealed class CFGVar implements CFGValue {
    private final String name;
    private final int version;
    private boolean shldTag;

    public CFGVar(String name, int version) {
        this.name = name;
        this.version = version;
        this.shldTag = false;
    }


    public boolean shouldTag() {
        return this.shldTag;
    }

    public void setShouldTag(boolean shldTag) {
        this.shldTag = shldTag;
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

    public boolean isTmp() {
        return name.equals("");
    }

    @Override public boolean equals(Object o) {
        try {
            return name.equals(((CFGVar)o).name()) && version == ((CFGVar)o).version;
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
non-sealed class CFGPrimitive implements CFGValue { 
    private static HashMap<Long, CFGPrimitive> primitives = new HashMap<>();
    private long value;

    private CFGPrimitive(long value) {
        this.value = value;
        primitives.put(value, this);
    }

    public static CFGPrimitive getPrimitive(long value)  {
        CFGPrimitive exst = primitives.get(value);
        if(exst == null)
            return new CFGPrimitive(value);
        return exst;
    }
    
    @Override public boolean equals(Object o) { return this == o; }
    
    @Override public String toString() {return ""+this.value; }

    public long value() {
        return this.value;
    } 
}

record CFGMethod(String name, CFGVar[] args, CFGVar[] locals, BasicBlock addr, ArrayList<BasicBlock> blocks, ArrayList<CFGVar> vars) implements CFGElement {
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

    public void addVar(CFGVar v) {
        vars.add(v);
    }

    public void condenseBlocks() {
        boolean changed = true;
        while(changed) {
            changed = false;
            for(BasicBlock b : blocks) {
                switch (b.getJmp()) {
                    case CFGAutoJumpOp a:
                        BasicBlock succ = a.target();
                        HashSet<BasicBlock> targetSuccs = succ.getSuccs();
                        if(succ.getPreds().size() == 1) { //b is only prececessor of succ
                            b.addOps(succ.getOps());
                            b.setJmp(succ.getJmp());
                            succ.setJmp(new CFGRetOp(CFGPrimitive.getPrimitive(0)));
                            b.removeSucc(succ);
                            for(BasicBlock s : ((HashSet<BasicBlock>)targetSuccs.clone())) {
                                s.replacePred(succ, b);
                            }
                            changed = true;
                        }
                        break;
                    case CFGCondOp c:
                        CFGExpr cond = c.cond();
                        if(cond instanceof CFGPrimitive) { //if will always evaluate to same value
                            changed = true;
                            long val = ((CFGPrimitive)cond).value();
                            BasicBlock target = val > 0 ? c.yes() : c.no(); //branch always taken
                            BasicBlock fakeBranch = val < 0 ? c.yes() : c.no(); //branch never taken
                            b.setJmp(new CFGAutoJumpOp(target));
                            b.removeSucc(fakeBranch);
                        }
                        break;
                    default: //return is unaffected
                        break;
                }
            }
            App.debug(this.toString());
            HashSet<BasicBlock> deadBlocks = new HashSet<>(); 
           for(BasicBlock b : blocks) {
                if(b.getPreds().size() == 0 && b != blocks.get(0)) {
                    deadBlocks.add(b);
                    
                    Iterator<BasicBlock> succIter = b.getSuccs().iterator();
                    for(int i = b.getSuccs().size() - 1; i >= 0; i--)
                        b.removeSucc(succIter.next());
                }
            }
            blocks.removeAll(deadBlocks);
        }
    }
}

record CFGArray(String name, Object[] elems) implements CFGData { int size()  {return this.elems.length; } @Override public String toString() { return "@"+name;} }

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
    public static ArrayList<BasicBlock> basicBlocks;
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

        basicBlocks = new ArrayList<>();
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
        HashSet<CFGVar> activeVars = new HashSet<>();
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
        ArrayList <CFGVar> vars = new ArrayList<>(Arrays.asList(args));
        vars.addAll(Arrays.asList(locals));
        BasicBlock start = new BasicBlock(blocksInMethod, m.name()+classname, m.body(), 0, activeVars, new HashSet<>(), tmp, locals, null);
        return new CFGMethod(m.name()+classname, args, locals, start, blocksInMethod, vars);
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

    public void toSSA(boolean simple) {
        HashMap<String, CFGVar> varMap;
        varMap = new HashMap<>();
        for(CFGVar v : main.vars())
            varMap.put(v.name(), v);
        setDominators(main.blocks());
        if (simple)
            mkSimplePhis(main.blocks()); // insert temp phis - simple ver
        else
            mkPhis(main.blocks()); // insert temp phis
        for (BasicBlock b : main.blocks())
            b.toSSA(varMap, new HashMap<>(varMap));

        for(CFGClass c : classes) {
            for(CFGMethod m : c.methods()) {
                varMap = new HashMap<>();
                for(CFGVar v : m.vars())
                    varMap.put(v.name(), v);
                setDominators(m.blocks());
                if(simple)
                    mkSimplePhis(m.blocks());
                else
                    mkPhis(m.blocks()); //insert temp phis
                for(BasicBlock b : m.blocks())
                    b.toSSA(varMap, new HashMap<>(varMap));
            }
        }
    }

    //calculate dominators, inverse dominators, nearest dominator, and dominance frontier for a set of blocks
    private void setDominators(ArrayList<BasicBlock> blocks) {
        HashSet<BasicBlock> allBlocks = new HashSet<>(blocks), tempDoms;
        blocks.get(0).addDominator(blocks.get(0));
        boolean changed = true;
        for(int i = 1; i < blocks.size(); i++) {
            blocks.get(i).setDominators(allBlocks);
        }
        while(changed) {
            changed = false;
            for(int i = 1; i < blocks.size(); i++) {
                BasicBlock b = blocks.get(i);
                tempDoms = new HashSet<>();
                for(BasicBlock p : b.getPreds()) {
                    if(tempDoms.size() == 0)
                        tempDoms.addAll(p.getDominators()); //initialize temp dominators to the dominators of the first pred
                    else
                        tempDoms.retainAll(p.getDominators()); //continually intersect temp with each successive pred
                }
                tempDoms.add(b); //add curr block to its own dominators
                if(!tempDoms.equals(b.getDominators())) {
                    changed = true;
                    b.setDominators(tempDoms);
                }
            }
        }
        for(BasicBlock b : blocks) {
            b.findNearestDominator();
	    for(BasicBlock d : b.dominators) { //add inverse dominators, this is maybe necessary but I'm not sure
                d.inverseDominators.add(b);
            }
        }
	    calcDominanceFrontiers(blocks);
    }

    private void calcDominanceFrontiers(ArrayList<BasicBlock> blocks) {
        BasicBlock tmp;
        for (BasicBlock b : blocks) {
            if (b.getPreds().size() <= 1)
                continue;
            for (BasicBlock p : b.getPreds()) {
                tmp = p;
                while (tmp != b.immediateDominator) {
                    tmp.dominanceFrontier.add(b);
                    tmp = tmp.immediateDominator;
                }
            }
        }
    }

    private void mkPhis(ArrayList<BasicBlock> blocks) {
        HashSet<CFGVar> globals = new HashSet<>(); // variables read aacross basic block
        HashMap<CFGVar, ArrayList<BasicBlock>> varBlocks = new HashMap<>(); //key = variable, val = blocks where variable is assigned
        HashSet<CFGVar> varKill; //vars assigned locally in-block
        ArrayList<BasicBlock> workList; // blocks needing phi work
        for (BasicBlock b : blocks) { // (incomplete) initial pass
            varKill = new HashSet<>();
            for (CFGOp c : b.getOps()) {
                switch (c) {
                    case CFGAssn a:
                        CFGVar out = a.var();
                        switch (a.expr()) {
                            case CFGBinOp n:
                                if (n.lhs() instanceof CFGVar && !varKill.contains(n.lhs()))
                                    globals.add((CFGVar) n.lhs());
                                if (n.rhs() instanceof CFGVar && !varKill.contains(n.rhs()))
                                    globals.add((CFGVar) n.rhs());
                                break;
                            case CFGVar v:
                                if (!varKill.contains(v))
                                    globals.add(v);
                                break;
                            case CFGCall l:
                                if (!varKill.contains(l.addr()))
                                    globals.add(l.addr());
                                if (!varKill.contains(l.receiver()))
                                    globals.add(l.receiver());
                                for (CFGValue x : l.args()) {
                                    if (x instanceof CFGVar && !varKill.contains(x))
                                        globals.add((CFGVar) x);
                                }
                                break;
                            case CFGGet g:
                                if (!varKill.contains(g.arr()))
                                    globals.add(g.arr());
                                if (g.val() instanceof CFGVar && !varKill.contains(g.val()))
                                    globals.add((CFGVar) g.val());
                                break;
                            default:
                                break;
                        }
                        varKill.add(out);
                        ArrayList<BasicBlock> blocksOut = varBlocks.get(out);
                        if(out.name().equals(""))
                            continue;
                        else if (blocksOut == null)
                            varBlocks.put(out, new ArrayList<>(Arrays.asList(b)));
                        else if (!blocksOut.contains(b))
                            blocksOut.add(b);
                        break;
                    case CFGSet s:
                        if(!varKill.contains(s.addr()))
                            globals.add(s.addr());
                        if(s.index() instanceof CFGVar && !varKill.contains(s.index()))
                            globals.add((CFGVar)s.index());
                        if(s.val() instanceof CFGVar && !varKill.contains(s.val()))
                            globals.add((CFGVar)s.val());
                        break;
                    case CFGPrint p:
                        if(p.val() instanceof CFGVar && !varKill.contains(p.val()))
                            globals.add((CFGVar)p.val());
                        break;
                    case CFGStore st:
                        if(!varKill.contains(st.base()))
                            globals.add(st.base());
                        if(st.index() instanceof CFGVar && !varKill.contains(st.index()))
                            globals.add((CFGVar)st.index());
                        break;
                    default:
                        break;
                }
            }
        }
        for(CFGVar v : globals) {
            
            workList = varBlocks.get(v);
            if(v.name().equals("this") || v.name().equals("") || varBlocks.get(v) == null)
                continue; //if var is this, temp, or not written across multiple blocks
            for(int i = 0; i < workList.size(); i++) {
                BasicBlock b = workList.get(i);
                for(BasicBlock d : b.dominanceFrontier) {
                    if(!d.hasPhi(v)) {
                        d.addPhi(v);
                        if(!workList.contains(d))
                            workList.add(d);
                    }
                    //stuff
                }
            }
        }        
    }

    private void mkSimplePhis(ArrayList<BasicBlock> blocks) {
        for(BasicBlock b : blocks) {
            if(b.getPreds().size() > 1) {
                for(CFGVar v : b.getActives()) {
                    if(!(v.isThis() || v.isTmp()))
                        b.addPhi(v);
                }
            }
        }
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

    public void localValueNumber() {
        for(BasicBlock b : main.blocks())
            b.doLocalValueNumbering();

        for(CFGClass c : classes)
            for(CFGMethod m : c.methods())
                for(BasicBlock b : m.blocks())
                    b.doLocalValueNumbering();
    }
    
    public void cleanBlocks() {
        main.condenseBlocks();

        for(CFGClass c : classes)
            for(CFGMethod m : c.methods())
                m.condenseBlocks();
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
    private HashSet<CFGVar> actives;
    private ArrayList<CFGOp> ops;
    private ArrayList <CFGAssn> phis; //phis stored in their own list for simplicity
    
    private CFGJumpOp jmp;
    private HashSet<BasicBlock> preds;
    private HashSet<BasicBlock> succs;
    
    
    public HashSet<BasicBlock> dominators; //blocks that dominate this block
    public HashSet<BasicBlock> inverseDominators; //blocks this block dominates
    public BasicBlock immediateDominator; //the "nearest" dominator in the CFG
    public HashSet<BasicBlock> dominanceFrontier; //blocks that *almost* dominate this block

    private static CFGVar tmp;
    private boolean inSSA; //boolean determining if block is already in SSA - used to avoid infinite loops


    public ArrayList<CFGOp> getOps() {
        return ops;
    }
    
    //replace predecessor "a" with "b"
    public void replacePred(BasicBlock a, BasicBlock b) {
       preds.remove(a);
       preds.add(b);
       a.succs.remove(this);
       b.succs.add(this);
    }

    public void setJmp(CFGJumpOp jmp) {
        this.jmp = jmp;
    }

    public void addOps(ArrayList<CFGOp> ops) {
        this.ops.addAll(ops);
    }

    public void removeSucc(BasicBlock s) {
        succs.remove(s);
        s.preds.remove(this);
    }

    public CFGJumpOp getJmp() {
       return jmp;
    }

    public void doLocalValueNumbering() {
        ArrayList<CFGExpr> vn = new ArrayList<>();
        ArrayList<CFGVar> names = new ArrayList<>(); //list of already-defined variables
        ArrayList<CFGOp> deadOps = new ArrayList<>();
        int index;
        boolean changed = true;
        while (changed) {
            changed = false;
            for (CFGOp o : ops) {
                switch (o) {
                    case CFGAssn a:
                        CFGVar precalc, v = a.var();
                        CFGExpr expr = a.expr();
                        if (expr instanceof CFGBinOp) //evaluate binary op (if both primitives) - basically poor-man's constant propagation
                            expr = ((CFGBinOp) expr).evalBinOp();
                        a.setExpr(expr);
                        if (expr instanceof CFGBinOp || expr instanceof CFGLoad) {
                            index = vn.indexOf(expr);
                            if (index != -1) {
                                changed = true;
                                precalc = names.get(index);
                                deadOps.add(a);
                                replaceGlobalUsages(new ArrayList<>(), v, precalc);
                                // traverse through block & replace exprs containing v with precalc
                            } else {
                                names.add(v);
                                vn.add(expr);
                            }
                            // alloc is ignored since classes need to be instantiated separately
                            // call & get are ignored since side effects exist
                            // var & primitive are handled separately
                            // phi is ignored since phis shouldn't be changed by VN (phis also won't be in
                            // Ops at this point)
                        } else if (expr instanceof CFGVar || expr instanceof CFGPrimitive) {
                            deadOps.add(a);
                            replaceGlobalUsages(new ArrayList<>(), v, (CFGValue) expr);
                        }
                        break;
                    default: // non-assignment operations are ignored
                        break;
                }
            }
            for (CFGOp o : deadOps) {
                ops.remove(o);
            }
        }
    }

    //replace ALL usages of oldVar in the method with newVar
    public void replaceGlobalUsages(ArrayList<BasicBlock> replaced, CFGVar oldVar, CFGValue newVar) {
        if(replaced.contains(this))
            return;
        replaced.add(this);
        this.replaceUsages(oldVar, newVar); //replace all usages in this block
        for(BasicBlock s : this.succs) {
            s.replaceGlobalUsages(replaced, oldVar, newVar); //replace usages in successors
        }
    }

    //replace usages of the CFGVar old with new (in expressions)
    public void replaceUsages(CFGVar oldVar, CFGValue newVar) {
        for(CFGAssn p : phis) {
             p.setExpr(replaceUsagesExpr(p.expr(), oldVar, newVar));
        }
        for(CFGOp o : ops) {
            switch (o) {
                //need cases for all op types calling replaceUsagesExpr on their exprs
                case CFGAssn a:
                    a.setExpr(replaceUsagesExpr(a.expr(), oldVar, newVar));
                    break;
                case CFGStore s:
                    s.setBase((CFGVar)replaceUsagesExpr(s.base(), oldVar, newVar));
                    s.setIndex((CFGData)replaceUsagesExpr(s.index(), oldVar, newVar));
                    break;
                case CFGSet s:
                    s.setAddr((CFGVar)replaceUsagesExpr(s.addr(), oldVar, newVar));
                    s.setIndex((CFGValue)replaceUsagesExpr(s.index(), oldVar, newVar));
                    s.setVal((CFGData)replaceUsagesExpr(s.val(), oldVar, newVar));
                    break;
                case CFGPrint p:
                    p.setVal((CFGValue)replaceUsagesExpr(p.val(), oldVar, newVar));
                    break;
                default:
                    break;
            }
        }
        switch (jmp) {
            case CFGRetOp r:
                r.setVal((CFGValue)replaceUsagesExpr(r.val(), oldVar, newVar));
                break;
            case CFGCondOp c:
                c.setCond((CFGValue)replaceUsagesExpr(c.cond(), oldVar, newVar));
                break;
            default:
                break;
        }
    }
    
    //replace usages of oldVar with newVar in the expression e
    public CFGExpr replaceUsagesExpr(CFGExpr e, CFGVar oldVar, CFGValue newVar) {
        switch(e) {
            case CFGVar v:
                return v.equals(oldVar) ? newVar : v;
            case CFGBinOp b:
                return new CFGBinOp(oldVar.equals(b.lhs()) ? newVar : b.lhs(), b.op(), oldVar.equals(b.rhs()) ? newVar : b.rhs());
            case CFGGet g:
                return new CFGGet(oldVar.equals(g.arr()) ? (CFGVar)newVar : g.arr(), oldVar.equals(g.val()) ? newVar : g.val());
            case CFGLoad l:
                return new CFGLoad(oldVar.equals(l.base()) ? (CFGVar)newVar : l.base());
            case CFGCall c:
                CFGValue[] newArgs = new CFGValue[c.args().length];
                for(int i = 0; i < newArgs.length; i++) {
                    CFGValue oldArg = c.args()[i];
                    newArgs[i] = oldVar.equals(oldArg) ? newVar : oldArg;
                }
                return new CFGCall(oldVar.equals(c.addr()) ? (CFGVar)newVar : c.addr(), 
                    oldVar.equals(c.receiver()) ? (CFGVar)newVar : c.receiver(), newArgs);
            case CFGPhi p:
                ArrayList<CFGValue> vars = p.varVersions();
                for(int i = 0; i < vars.size(); i++) {
                    CFGValue var = vars.get(i);
                    vars.set(i, oldVar.equals(var) ? newVar : var);
                }
                return p;
            default: //CFGPrimitive, 
                return e;

        }
    }

    public void addPhi(CFGVar v) {
        ArrayList<CFGValue> vars = new ArrayList<>();
        ArrayList<BasicBlock> blocks = new ArrayList<>();
        for(BasicBlock p : preds) {
            vars.add(v);
            blocks.add(p);
        }
        CFGAssn newPhi = new CFGAssn(v, new CFGPhi(blocks, vars));
        phis.add(newPhi);
    }
    
    public boolean hasPhi(CFGVar v) {
        for(CFGAssn p : phis) {
            if(p.var().equals(v))
                return true;
        }
        return false;
    }
    
    public ArrayList<CFGAssn> getPhis() {
        return phis;
    }

    public BasicBlock(ArrayList<BasicBlock> blocksInMethod, String blockBaseName, ArrayList <Statement> stmts, int startIndex, HashSet<CFGVar> actives, 
        HashSet<BasicBlock> preds, CFGVar tmp, CFGVar[] locals, BasicBlock jmpBack) {
            this(blocksInMethod, tmp, preds, actives);
            this.setupBlock(blocksInMethod, blockBaseName, stmts, startIndex, locals, jmpBack);
        }
    
    public HashSet<BasicBlock> getSuccs() {
        return succs;
    }

    public BasicBlock(ArrayList<BasicBlock> blocksInMethod, CFGVar tmp) { //placeholder constructor to just initialize arraylists
        if(tmp != null)
            BasicBlock.tmp = tmp;
        inSSA = false;
        currBlock = this;
        blocksInMethod.add(this);
        CtrlFlowGraph.basicBlocks.add(this);
        preds = new HashSet<>();
        succs = new HashSet<>();
        actives = new HashSet<>();
        dominators = new HashSet<>();
	    inverseDominators = new HashSet<>();
        dominanceFrontier = new HashSet<>();
        ops = new ArrayList<>();
        phis = new ArrayList<>();
        jmp = null;
        return;
    }

    // create empty basic block with predecessor & active var setup
    // used where basic blocks are built manually (while, method call, field r/w)
    public BasicBlock(ArrayList<BasicBlock> blocksInMethod, CFGVar tmp, HashSet<BasicBlock> preds, HashSet<CFGVar> actives) {
        this(blocksInMethod, tmp);
        setPredsActives(preds, actives);
    }

    //create a fail block
    public BasicBlock(ArrayList<BasicBlock> blocksInMethod, CFGVar tmp, CFGFailOpt failType, HashSet<BasicBlock> preds) {
        this(blocksInMethod, tmp);
        this.preds = new HashSet<>(preds);
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
        HashSet<BasicBlock> localPreds = new HashSet<>();
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
                    CFGExpr operand = exprToCFG(assignment, blocksInMethod, blockBaseName, a.rhs(), locals, false);
                    //CFGExpr tagged = operand;
                    //if(operand instanceof CFGBinOp) {
                    //    tmp = new CFGVar(tmp);
                    //    tmp.setShouldTag(true);
                    //    tagged = tmp;
                    //    currBlock.addOp(new CFGAssn((CFGVar)tagged, operand));    
                    //}
                    if(assignment == null)
                        throw new IllegalArgumentException("Post-Parse error: Cannot initialize variable "+name+" as it was neither passed as an argument nor declared as a local.");
                    if(!(operand instanceof CFGBinOp))
                        operand = tagInts(assignment, operand);
                    currBlock.addOp(new CFGAssn(assignment, operand));
                    break;
                case IfElseStmt ie:
                    cond = (CFGValue)exprToCFG(null, blocksInMethod, blockBaseName, ie.cond(), locals, true);
                    if(cond instanceof CFGVar && !((CFGVar)cond).isTmp())
                        cond = checkUntagInt(null, (CFGVar)cond, blocksInMethod, blockBaseName, localPreds);
                    localPreds.add(currBlock);
                    branchEntryBlock = currBlock;
                    ifBlk = new BasicBlock(blocksInMethod, null);
                    if(i < stmts.size()-1) {
                        afterIf = new BasicBlock(blocksInMethod, null);
                        blocksInMethod.remove(blocksInMethod.indexOf(afterIf));
                    }
                    ifBlk.setPredsActives(localPreds, actives);
                    
                    currBlock = ifBlk;
                    ifBlk.setupBlock(blocksInMethod, blockBaseName, ie.body(), 0, locals, afterIf); 
                    BasicBlock endIf = currBlock;
                    BasicBlock elseBlk = new BasicBlock(blocksInMethod, null);
                    elseBlk.setPredsActives(localPreds, actives);
                    currBlock = elseBlk;
                    elseBlk.setupBlock(blocksInMethod, blockBaseName, ie.elseBody(), 0, locals, afterIf);
                    localPreds.remove(branchEntryBlock);
                    localPreds.add(currBlock);
                    localPreds.add(endIf);
                    if(afterIf != null){
                        blocksInMethod.add(afterIf);
                        currBlock = afterIf;
                        afterIf.setPredsActives(localPreds, actives);
                        afterIf.setupBlock(blocksInMethod, blockBaseName, stmts, i + 1, locals, jmpBack);
                    }
                    branchEntryBlock.jmp = new CFGCondOp(cond, ifBlk, elseBlk);
                    localPreds.clear();
                    return;
                case IfOnlyStmt io:
                    cond = (CFGValue)exprToCFG(null, blocksInMethod, blockBaseName, io.cond(), locals, true);
                    if(cond instanceof CFGVar && !((CFGVar)cond).isTmp())
                        cond = checkUntagInt(null, (CFGVar)cond, blocksInMethod, blockBaseName, localPreds);
                    localPreds.add(currBlock);
                    branchEntryBlock = currBlock;
                    ifBlk = new BasicBlock(blocksInMethod, null);
                    ifBlk.setPredsActives(localPreds, actives);
                    currBlock = ifBlk;
                    ifBlk.setupBlock(blocksInMethod, blockBaseName, io.body(), 0, locals, afterIf);
                    afterIf = new BasicBlock(blocksInMethod, null);
                    afterIf.setPredsActives(localPreds, actives);
                    afterIf.setupBlock(blocksInMethod, blockBaseName, stmts, i+1, locals, jmpBack);
                    branchEntryBlock.jmp = new CFGCondOp(cond, ifBlk, afterIf);
                    localPreds.clear();
                    return;
                case WhileStmt w:
                    branchEntryBlock = currBlock;
                    localPreds.add(branchEntryBlock);
                    BasicBlock loopheadStart = new BasicBlock(blocksInMethod, null);
                    loopheadStart.setPredsActives(localPreds, actives);
                    localPreds.remove(branchEntryBlock);
                    loopheadStart.setIdentifier(blockBaseName);
                    cond = (CFGValue)exprToCFG(null, blocksInMethod, blockBaseName, w.cond(), locals, true);
                    if(cond instanceof CFGVar && !((CFGVar)cond).isTmp())
                        cond = checkUntagInt(null, (CFGVar)cond, blocksInMethod, blockBaseName, localPreds);
                    BasicBlock loopheadEnd = currBlock;
                    localPreds.add(loopheadEnd);
                    loopheadEnd.addActives(actives);
                    BasicBlock body = new BasicBlock(blocksInMethod, blockBaseName, w.body(), 0, actives, localPreds, null, locals, loopheadStart);
                    currBlock.succs.add(loopheadStart);
                    BasicBlock after = new BasicBlock(blocksInMethod, blockBaseName, stmts , i + 1, actives, localPreds, null, locals, jmpBack);
                    localPreds.remove(loopheadEnd);
                    loopheadEnd.addJump(new CFGCondOp(cond, body, after)); //add jump at end
                    branchEntryBlock.jmp = new CFGAutoJumpOp(loopheadStart);  
                    return;
                case PrintStmt p:
                    CFGValue prt = (CFGValue)exprToCFG(null, blocksInMethod, blockBaseName, p.str(), locals, true);
                    if(prt instanceof CFGVar) { //need to untag int & make sure we're not dereferencing a ptr
                        prt = checkUntagInt(null, (CFGVar)prt, blocksInMethod, blockBaseName, localPreds);                        
                    }
                    currBlock.addOp(new CFGPrint(prt));
                    break;
                case FieldWriteStmt f: // can break if writing ptr to field
                    CFGValue objToStore = (CFGValue)currBlock.exprToCFG(null, blocksInMethod, blockBaseName, f.rhs(), locals, true); //evaluate rhs first
                    //can safely cast obj since it is known to be a var identifier by tokenizer
                    CFGVar obj = (CFGVar)exprToCFG(null, blocksInMethod, blockBaseName, f.base(), locals, true);
                    int fieldId = CtrlFlowGraph.getFieldId(f.fieldname()); //get index of field in fields arr
                    if(fieldId == -1)
                        throw new IllegalArgumentException("Attempt to modify never-declared field "+f.fieldname());
                    genPtrTagChk(obj, blocksInMethod, blockBaseName, localPreds); //gen tag check for obj
                    BasicBlock getField = currBlock;
                    tmp = new CFGVar(tmp);
                    CFGVar fieldsAddr = tmp;
                    tmp = new CFGVar(tmp);
                    CFGVar fields = tmp;
                    getField.addOp(new CFGAssn(fieldsAddr, new CFGBinOp(obj, "+", CFGPrimitive.getPrimitive(8))));
                    getField.addOp(new CFGAssn(fields, new CFGLoad(fieldsAddr)));
                    tmp = new CFGVar(tmp);
                    CFGVar field = tmp;
                    getField.addOp(new CFGAssn(field, new CFGGet(fields, CFGPrimitive.getPrimitive(fieldId))));
                    localPreds.remove(currBlock);
                    localPreds.add(getField);
                    BasicBlock fieldFail = new BasicBlock(blocksInMethod, null, CFGFailOpt.NoSuchField, localPreds);
                    BasicBlock storeVar = new BasicBlock(blocksInMethod, null, localPreds, actives);
                    getField.jmp = new CFGCondOp(field, storeVar, fieldFail); //test for no such field
                    if(objToStore instanceof CFGPrimitive)
                        objToStore = (CFGValue)tagInt(null, objToStore);
                    currBlock.addOp(new CFGSet(obj, field, objToStore));
                    storeVar.setupBlock(blocksInMethod, blockBaseName, stmts, i + 1, locals, jmpBack);
                    return; //rest of method is processed by setupBlock
                case ReturnStmt r:
                    CFGValue valToReturn = (CFGValue)exprToCFG(null, blocksInMethod, blockBaseName, r.output(), locals, true);
                    if(valToReturn instanceof CFGPrimitive)
                        valToReturn = (CFGValue)tagInt(null, valToReturn);
                    currBlock.jmp = new CFGRetOp(valToReturn);
                    break;
                case VoidStmt v:
                    CFGValue voidRslt = (CFGValue)exprToCFG(null, blocksInMethod, blockBaseName, v.rhs(), locals, true);
                    tmp = new CFGVar(tmp);
                    currBlock.addOp(new CFGAssn(tmp, voidRslt));
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
                currBlock.jmp = new CFGRetOp(CFGPrimitive.getPrimitive(0));
            else {
                jmpBack.addPred(currBlock);
                currBlock.jmp = new CFGAutoJumpOp(jmpBack);
            }
        }
        return;
    }

    public void toSSA(HashMap<String, CFGVar> varMap, HashMap<String, CFGVar> maxVer) {
        if(inSSA)
            return;
        inSSA = true;
        for(CFGAssn phi : phis) {
            CFGVar oldVer = maxVer.get(phi.var().name());
            CFGVar newVer = new CFGVar(oldVer);
            phi.setVar(newVer);
            varMap.replace(oldVer.name(), newVer);
            maxVer.replace(oldVer.name(), newVer);
        }
        for(CFGOp o : ops) {
            opToSSA(o, varMap, maxVer);
        }
        jumpToSSA(varMap);
        for(BasicBlock succ : succs) { //put all succs of this block that it also dominates into SSA
            HashMap<String, CFGVar> outVars = new HashMap<>(varMap);
            for (CFGAssn a : succ.phis) {
                CFGVar phiVar = a.var();
                CFGVar updatedVar = outVars.get(phiVar.name());
                CFGPhi phiOp = (CFGPhi)a.expr();
                for(int i = 0; i < phiOp.blocks().size(); i++) {
                    if(phiOp.blocks().get(i) == this) {
                        phiOp.varVersions().set(i, updatedVar);
                        if(updatedVar.version() == -1) {
                            System.out.println("Error: Variable "+updatedVar.name()+" may be used before being initialized.");
                            System.exit(1);
                        }
                    }
                    
                }
            }
            if(inverseDominators.contains(succ)) {
                succ.toSSA(outVars, maxVer);
            }
        }
    }

    void opToSSA (CFGOp o, HashMap<String, CFGVar> varMap, HashMap<String, CFGVar> maxVer) {
        switch (o) {
            case CFGAssn a:
                a.setExpr(exprToSSA(a.expr(), varMap));
                // do whatever thing needs to be added for exprs
                CFGVar base = a.var();
                CFGVar storedVar = varMap.get(base.name());
                if (storedVar == null) // assignment to temporary value
                    return;
                CFGVar newVar = new CFGVar(storedVar);
                a.setVar(newVar);
                varMap.replace(storedVar.name(), newVar);
                maxVer.replace(newVar.name(), newVar);
                actives.add(newVar);
                break;
            case CFGPrint p:
                p.setVal((CFGValue) exprToSSA(p.val(), varMap));
                break;
            case CFGSet s:
                s.setAddr((CFGVar) exprToSSA(s.addr(), varMap));
                s.setIndex((CFGValue) exprToSSA(s.index(), varMap));
                s.setVal((CFGData) exprToSSA(s.val(), varMap));
                break;
            case CFGStore s:
                s.setIndex((CFGData) exprToSSA(s.index(), varMap));
                s.setBase((CFGVar) exprToSSA(s.base(), varMap));
                break;
            default:
                break;
        }
    }

    void jumpToSSA(HashMap<String, CFGVar> varMap) {
        switch(jmp) {
            case CFGCondOp c:
                c.setCond((CFGValue)exprToSSA(c.cond(), varMap));
                break;
            case CFGRetOp r:
                r.setVal((CFGValue)exprToSSA(r.val(), varMap));
            default:
                break;
        }
    }
    
    CFGExpr exprToSSA(CFGExpr expr, HashMap<String, CFGVar> varMap) {
        switch(expr) {
            case CFGVar v:
                if(v.name().equals("") || v.name().equals("this"))
                    return expr;
                CFGVar currVer = varMap.get(v.name());
                return currVer;
            case CFGPrimitive c:
                return expr;
            case CFGBinOp b:
                CFGValue left = (CFGValue)exprToSSA(b.lhs(), varMap);
                CFGValue right = (CFGValue)exprToSSA(b.rhs(), varMap);
                CFGExpr currExpr = new CFGBinOp(left, b.op(), right);
                return currExpr;
            case CFGCall c:
                CFGVar newAddr = (CFGVar)exprToSSA(c.addr(), varMap);
                CFGVar newReceiver = (CFGVar)exprToSSA(c.receiver(), varMap);
                CFGValue[] newArgs = new CFGValue[c.args().length];
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
    private void setPredsActives(HashSet<BasicBlock> preds, HashSet<CFGVar> actives) {
        this.preds.clear();
        this.succs.clear();
        this.actives.clear();
        this.preds.addAll(preds);
        if(preds.size() == 0) //if this is the starting pt of the method
            this.actives.addAll(actives);
        else
            this.actives.addAll(preds.iterator().next().getActives());
        for(BasicBlock p : this.preds) {
            p.succs.add(this);
        }
    }

    public void findNearestDominator() {
        ArrayDeque<BasicBlock> bfsQueue = new ArrayDeque<>();
        bfsQueue.add(this);
        BasicBlock curr;
        while (!bfsQueue.isEmpty()) {
            curr = bfsQueue.remove();
            if (curr != this && dominators.contains(curr)) {
                immediateDominator = curr;
                return;
            }
            for (BasicBlock p : curr.getPreds()) {
                bfsQueue.add(p);
            }
            
        }
        immediateDominator = null;
    }

    //print block as a String
    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        for(CFGAssn phi : phis)
            sb.append("\t" + phi + "\n");
        for(CFGOp op : ops) {
            sb.append("\t" + op + "\n");
        }
        sb.append("\t" + jmp);
        return sb.toString();
    }

    public HashSet<CFGVar> getActives() {
        return actives;
    }

    public void addSucc(BasicBlock b) {
        succs.add(b);
    }

    //overwrite existing actives and replace it with v - intended to be use to temporarily pre-initialize in cases where loops are being turned into CFG
    public void addActives(HashSet<CFGVar> v) {
        actives = v;
    }

    public void addOp(CFGOp c) {
        ops.add(c);
    }
    
    public void prependOp(CFGOp c) {
        ops.add(0, c);
    }

    private void addJump(CFGJumpOp j) {
        jmp = j;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void addPred(BasicBlock pred) {
        if(!preds.contains(pred)) {
            preds.add(pred);
        }
    }

    public HashSet<BasicBlock> getPreds() {
        return this.preds;
    }

    public HashSet<BasicBlock> getDominators() {
        return this.dominators;
    }

    public void setDominators(HashSet<BasicBlock> dominatorSet) {
        this.dominators = new HashSet<>();
        dominators.addAll(dominatorSet);
    }
    
    public void addDominator(BasicBlock b) {
        this.dominators.add(b);
    }

    //generate a pointer tag check for the variable var
    public void genPtrTagChk(CFGVar obj, ArrayList<BasicBlock> blocksInMethod, String blockBaseName, HashSet<BasicBlock> preds) {
        HashSet<BasicBlock> localPreds = new HashSet<>(preds);
        if(obj.isThis())
            return; //PEEPHOLE OPT - DONT GENERATE TAG CHECKS FOR THIS
        tmp = new CFGVar(tmp);
        CFGVar objAddr = tmp;
        currBlock.addOp(new CFGAssn(tmp, new CFGBinOp(obj, "&", CFGPrimitive.getPrimitive(1)))); // get LSB
        localPreds.add(currBlock);
        BasicBlock beforeBranch = currBlock;
        BasicBlock notPtr = new BasicBlock(blocksInMethod, null, CFGFailOpt.NotAPointer, localPreds); // basic block for not a ptr
        BasicBlock isPtr = new BasicBlock(blocksInMethod, null, localPreds, actives); // basic block for ptr is real
        beforeBranch.jmp = new CFGCondOp(objAddr, notPtr, isPtr);
        isPtr.setIdentifier(blockBaseName);
    }

    public void genIntTagChk(CFGVar var, ArrayList<BasicBlock> blocksInMethod, String blockBaseName, HashSet<BasicBlock> localPreds){
        if(var.isThis())
            return; //PEEPHOLE OPT - DONT GENERATE TAG CHECKS FOR THIS
        tmp = new CFGVar(tmp);
        CFGVar intOrPtr = tmp;
        currBlock.addOp(new CFGAssn(intOrPtr, new CFGBinOp(var, "&", CFGPrimitive.getPrimitive(1)))); // if intOrPtr=1, we have int. else, we have ptr.
        localPreds.add(currBlock);
        BasicBlock branchBlock = currBlock;
        BasicBlock nanFail = new BasicBlock(blocksInMethod, tmp, CFGFailOpt.NotANumber, localPreds);
        BasicBlock isInt = new BasicBlock(blocksInMethod, intOrPtr, localPreds, actives);
        isInt.setIdentifier(blockBaseName);
        branchBlock.addJump(new CFGCondOp(intOrPtr, isInt, nanFail));
    }

    //tag constant integers as they're being assigned to variables - detagged when printing
    public CFGExpr tagInts(CFGVar out, CFGExpr expr) {
        switch (expr) {
            case CFGPrimitive c:
                return tagInt(out, c);
            case CFGVar v:
                return v.shouldTag() ? tagInt(out, v) : v;
            case CFGBinOp b:
                return new CFGBinOp((CFGValue)tagInts(null, b.lhs()), b.op(), (CFGValue)tagInts(null, b.rhs()));
            default: //anything other than a const or a binop
                return expr;
        }
    }

    public CFGExpr tagInt(CFGVar out, CFGValue val) {
        tmp = new CFGVar(tmp);
        CFGVar lsft = tmp;
        currBlock.addOp(new CFGAssn(lsft, new CFGBinOp(val, "<<", CFGPrimitive.getPrimitive(1))));
        CFGExpr incr = new CFGBinOp(lsft, "+", CFGPrimitive.getPrimitive(1));
        if (out == null) {
            tmp = new CFGVar(tmp);
            CFGVar lsftPlus = tmp;
            currBlock.addOp(new CFGAssn(lsftPlus, incr));
            return lsftPlus;
        }
        return incr;
    }

    //given an int var in, generate code to tag check and untag the int, returning the untagged variable out
    //if out is null, return a new temp - otherwise, assign the value to out and return it
    public CFGVar checkUntagInt(CFGVar out, CFGVar in, ArrayList<BasicBlock> blocksInMethod, String blockBaseName, HashSet<BasicBlock> localPreds) {
        genIntTagChk(in, blocksInMethod, blockBaseName, localPreds);
        localPreds.clear();
        return doUntag(out, in);
    }

    public CFGVar doUntag(CFGVar out, CFGVar in) {
        tmp = new CFGVar(tmp);
        CFGVar minus1 = tmp;
        currBlock.addOp(new CFGAssn(minus1, new CFGBinOp(in, "-", CFGPrimitive.getPrimitive(1))));
        if(out == null) {
            tmp = new CFGVar(tmp);
            out = tmp;
        }
        currBlock.addOp(new CFGAssn(out, new CFGBinOp(minus1, ">>", CFGPrimitive.getPrimitive(1))));
        return out;
    }

    //convert a potentially complex CFG expr into a series of statements
    public CFGExpr exprToCFG(CFGVar assn, ArrayList<BasicBlock> blocksInMethod, String blockBaseName, Expression expr, CFGVar[] locals, boolean requireVal) {
        HashSet<BasicBlock> localPreds = new HashSet<>();
        BasicBlock badFieldBlock, badMethodBlock;
        CFGExpr out;
        switch (expr) {
            case Constant c:
                return CFGPrimitive.getPrimitive(c.value());
            case Variable v:
                CFGVar tmpVar = getActive(v.name());
                if(tmpVar == null)
                    throw new IllegalArgumentException("Attempted to access nonexistent or uninitialized variable "+v.name() + " (expr "+expr+")\n\nCURR CFG:\n\n"+App.cfg);
                return tmpVar;
            case Binop b:
                CFGExpr lhs, rhs;
                lhs = exprToCFG(null, blocksInMethod, blockBaseName, b.lhs(), locals, true); 
                rhs = exprToCFG(null, blocksInMethod, blockBaseName, b.rhs(), locals, true);
                if(lhs instanceof CFGPrimitive && rhs instanceof CFGPrimitive) { //optimize out double-constant binops
                    CFGPrimitive lprim = (CFGPrimitive)lhs;
                    CFGPrimitive rprim = (CFGPrimitive)rhs;
                    long rslt;
                    switch (b.op()) {
                        case "+": rslt = lprim.value()+rprim.value(); break;
                        case "-": rslt = lprim.value()-rprim.value(); break;
                        case "*": rslt = lprim.value()*rprim.value(); break;
                        case "/": rslt = lprim.value()/rprim.value(); break;
                        case ">": rslt = lprim.value() > rprim.value() ? 1 : 0; break;
                        case "<": rslt = lprim.value() < rprim.value() ? 1 : 0; break;
                        case "<<": rslt = lprim.value()<<rprim.value(); break;
                        case ">>": rslt = lprim.value()>>rprim.value(); break;
                        case "<=": rslt = lprim.value()<=rprim.value() ? 1 : 0; break;
                        case ">=": rslt = lprim.value()>=rprim.value() ? 1 : 0; break;
                        case "==": rslt = lprim.value()==rprim.value() ? 1 : 0; break;
                        case "!=": rslt = lprim.value()!=rprim.value() ? 1 : 0; break;
                        default: //should be unreachable
                            rslt = 0;
                    }
                    return CFGPrimitive.getPrimitive(rslt);
                }
                if(lhs instanceof CFGVar) {
                    if(!b.isBool())
                        lhs = checkUntagInt(null, (CFGVar)lhs, blocksInMethod, blockBaseName, localPreds);
                    else
                        lhs = doUntag(null, (CFGVar)lhs);
                }
                if(rhs instanceof CFGVar) {
                    if(!b.isBool())
                        rhs = checkUntagInt(null, (CFGVar)rhs, blocksInMethod, blockBaseName, localPreds);
                    else
                        rhs = doUntag(null, (CFGVar)rhs);
                
                }
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

                out = new CFGBinOp((CFGValue)lhs, b.op(), (CFGValue)rhs);
                if(!b.isBool()) {
                    tmp = new CFGVar(tmp);
                    CFGVar pretag = tmp;
                    currBlock.addOp(new CFGAssn(pretag, out));
                    out = tagInt(assn, pretag);
                }
                break;
            case ClassRef c: //used for class reference in a complex expression, so we need to return an anonymous(temp) value
                CFGClass classData = CtrlFlowGraph.findClass(c.classname());
                CFGVar cRef = assn;
                if(classData == null)
                    throw new IllegalArgumentException("Class "+c.classname()+" is undefined");
                if(cRef == null) {
                    tmp = new CFGVar(tmp);
                    cRef = tmp;
                    actives.add(tmp);
                }
                currBlock.addOp(new CFGAssn(cRef, new CFGAlloc(CFGPrimitive.getPrimitive(classData.numFields()+2)))); //alloc vtable, field map, fields
                currBlock.addOp(new CFGStore(cRef, classData.vtable()));
                tmp = new CFGVar(tmp);
                currBlock.addOp(new CFGAssn(tmp, new CFGBinOp(cRef, "+", CFGPrimitive.getPrimitive(8))));
                currBlock.addOp(new CFGStore(tmp, classData.fields()));
                out = cRef;
                break;
            case FieldRead f:
                int expectedFieldId = CtrlFlowGraph.getFieldId(f.fieldname());
                if(expectedFieldId == -1)
                    throw new IllegalArgumentException("Code attempts to read from never-defined field "+f.fieldname());
                CFGVar obj = (CFGVar)exprToCFG(null, blocksInMethod, blockBaseName, f.base(), locals, true);
                genPtrTagChk(obj, blocksInMethod, blockBaseName, localPreds); //gen tag check for obj
                BasicBlock getFieldId = currBlock;
                tmp = new CFGVar(tmp);
                CFGVar fieldAddr = tmp;
                getFieldId.addOp(new CFGAssn(fieldAddr, new CFGBinOp(obj, "+", CFGPrimitive.getPrimitive(8))));
                tmp = new CFGVar(tmp);
                CFGVar fieldMap = tmp;
                getFieldId.addOp(new CFGAssn(fieldMap, new CFGLoad(fieldAddr)));
                tmp = new CFGVar(tmp);
                CFGVar fieldOffset = tmp; //used to get the offset of the field
                getFieldId.addOp(new CFGAssn(fieldOffset, new CFGGet(fieldMap, CFGPrimitive.getPrimitive(expectedFieldId))));
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
                obj = (CFGVar)exprToCFG(null, blocksInMethod, blockBaseName, m.base(), locals, true);
                genPtrTagChk(obj, blocksInMethod, blockBaseName, localPreds); //gen tag check for obj
                BasicBlock getMethodId = currBlock;
                //load vtable, find method
                tmp = new CFGVar(tmp);
                CFGVar vtbl = tmp;
                getMethodId.addOp(new CFGAssn(vtbl, new CFGLoad(obj)));
                tmp = new CFGVar(tmp);
                CFGVar methodAddr = tmp;
                getMethodId.addOp(new CFGAssn(methodAddr, new CFGGet(vtbl, CFGPrimitive.getPrimitive(methodId)))); //get vtable id
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
                    args[i] = (CFGValue)exprToCFG(null, blocksInMethod, blockBaseName, e, locals, true);
                    if(args[i] instanceof CFGPrimitive)
                        args[i] = (CFGValue)tagInt(null, args[i]);
                }

                callBlock.addOp(new CFGAssn(callRslt, new CFGCall(methodAddr, obj, args))); //figure out receiver
                out = callRslt;
                break;
            case ThisExpr t:
                return getActive("this");
            default:
                return null;
        }
        if(requireVal && !(out instanceof CFGVar)) {
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
    static boolean debug = false;
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: <comp> infile [-o outfile] [args...]");
            System.exit(1);
        }
        String inFilePath = args[0];
        String outFilePath = "";
        boolean ssa = true, outName = false, simple = false, vn = true;
        if(args.length >= 3 && args[1].equals("-o")) {
            
        }

        for(int nextArg =  1; nextArg < args.length; nextArg++) {
            switch (args[nextArg]) {
                case "-noSSA":
                    ssa = false;
                    break;
                case "-simpleSSA":
                    simple = true;
                    break;
                case "-noVN":
                    vn = false;
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
                case "-d":
                    debug = true;
                    break;
                default:
                    System.out.print("Command-line arg "+args[nextArg]+" not recognized");
                    System.exit(1);
            }
        }
        vn = vn && ssa;
        String code = "";
        try {
            code = Files.readString(Path.of("test-code/"+inFilePath), StandardCharsets.UTF_8);
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
            cfg.toSSA(simple);
        if(vn)
            cfg.localValueNumber();
        cfg.cleanBlocks();
        if(outFilePath == "") {
            System.out.println(cfg);
            return;
        }
        try {
            Files.write(Path.of("test-out/"+outFilePath), cfg.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch(Exception e) {
            System.err.println("Cannot write code to file "+outFilePath);
            e.printStackTrace();
        }
    }

    public static void debug(String... str) {
        if(debug)
            System.out.println(str);
    }
}
