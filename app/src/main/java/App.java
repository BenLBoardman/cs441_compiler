import java.lang.StringBuilder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import java.nio.file.Path;
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
                        if(eql.getType() != TokenType.OPERATOR && ((Operator) eql).getOp().equals("="))
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
        return new Method(name, null, locals, body);
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
    permits CFGOp, CFGJumpOp, CFGMethod, CFGData {}

sealed interface CFGData extends CFGElement
    permits CFGValue, CFGArray {}

sealed interface CFGValue extends CFGData
    permits CFGVar, CFGPrimitive {}

sealed interface CFGOp extends CFGElement
    permits CFGPhi, CFGUnaryAssn, CFGBinaryAssn, CFGPrint, CFGSet, CFGStore, CFGGet, CFGAlloc, CFGCall, CFGLoad {
        //public CFGOpType getType();
}

record CFGPrint(CFGValue val) implements CFGOp { @Override public String toString() { return "print("+val+")"; } }
record CFGUnaryAssn(CFGVar var, CFGValue val) implements CFGOp { @Override public String toString() { return var + " = " + val; } }
record CFGBinaryAssn(CFGVar res, CFGValue lhs, String op, CFGValue rhs) implements CFGOp {@Override public String toString() { return res + " = " + lhs + " " + op + " " + rhs; } }
record CFGGet(CFGVar out, CFGVar arr, CFGValue val) implements CFGOp { @Override public String toString() {return out + " = getelt(" + arr + ", " + val + ")"; } }
record CFGSet(CFGVar addr, CFGData i, CFGData i2) implements CFGOp { @Override public String toString() {return "setelt("+addr+", "+i+", "+i2+")"; } }
record CFGAlloc(CFGVar out, CFGPrimitive size) implements CFGOp { @Override public String toString() { return out + " = alloc("+size+")"; } }
record CFGCall(CFGVar out, CFGVar addr, CFGVar receiver, ArrayList<CFGValue> args) implements CFGOp {} //todo tostring
record CFGLoad(CFGVar out, CFGVar base) implements CFGOp { @Override public String toString() { return out + " = load(" + base + ")"; } }
record CFGStore(CFGVar base, CFGData i) implements CFGOp { @Override public String toString() { return "store(" +base+", "+i+ ")"; } }

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


record CFGVar(String name, int version) implements CFGValue {
    public CFGVar(CFGVar prev) {
        this(prev.name, prev.version + 1);
    }

    public CFGVar(String name) {
        this(name, 0);
    }

    @Override
    public final String toString() {
        return "%"+this.name+this.version;
    }
}
record CFGPrimitive(long value) implements CFGValue { @Override public String toString() {return ""+this.value; } }
record CFGMethod(String name, CFGVar[] args, BasicBlock addr) implements CFGElement {}
record CFGArray(String name, Object[] elems) implements CFGData { int size()  {return this.elems.length; } @Override public String toString() { return "@"+name;} }

record CFGPhi(CFGVar out, ArrayList<BasicBlock> blocks, ArrayList<CFGVar> varVersions) implements CFGOp {
    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(out).append(" = phi(");
        for(int i = 0; i < blocks.size(); i++) {
            sb.append(blocks.get(i).getIdentifier()).append(", ");
            sb.append(varVersions.get(i)).append((i < blocks.size() - 1) ? ", " : "");
        }
        return sb.append(")").toString();
    }
}

record CFGClass(String name, CFGArray fields, CFGArray vtable, int numFields) { }



class CtrlFlowGraph {
    public static ArrayList<BasicBlock> CFGCodeBlock;
    public static DataBlock CFGDataBlock;
    public static ArrayList<String> globals;
    public static ArrayList<String> methods;
    public static ArrayList<CFGClass> classes;
    public static ParsedCode parsedCode;

    public CtrlFlowGraph (ParsedCode code) {
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
            for(int i = 0; i < uniqueFields.size(); i++) {
                if(c.fields().contains(uniqueFields.get(i))) {
                    fields.elems()[i] = 2;
                }
                else {
                    fields.elems()[i] = 0;
                }
                
            }
            CFGDataBlock.data().add(vtable);
            CFGDataBlock.data().add(fields);
            classes.add(new CFGClass(c.name(), fields, vtable, c.fields().size()));
        }

        CFGCodeBlock = new ArrayList<>();
        for(Class c : code.classes) {
            for(Method m : c.methods()) {
                methodToCfg(m);
            }
        }

        methodToCfg(code.main);

        //then process main method by recursively parsing called methods
    }

    public static CFGClass findClass(String s) {
        for(CFGClass c : classes) {
            if(c.name().equals(s))
                return c;
        }
        return null;
    }
    
    private void methodToCfg(Method m) {
        CFGVar tmp = new CFGVar("");
        ArrayList<CFGVar> activeVars = new ArrayList<>();
        if(m.args() != null) {
            for (String s : m.args()) {
                activeVars.add(new CFGVar(s));
            }
        }
        BasicBlock.blockId = 0;
        new BasicBlock(m.name(), m.body(), 0, activeVars, new ArrayList<>(), tmp, m.locals(), null);

    }


    public static CFGVar getActive(ArrayList<CFGVar> actives, String varName) {
        for(CFGVar v : actives) {
            if(v.name().equals(varName)) {
                return v;
            }
        }
        return null;
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(CFGDataBlock);
        sb.append("code:\n\n");
        for(BasicBlock b : CFGCodeBlock) {
            sb.append(b).append('\n');
        }
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

    private String identifier;
    private ArrayList<CFGVar> actives;
    private ArrayList<CFGOp> ops;
    private CFGJumpOp jmp;
    private ArrayList<BasicBlock> preds;
    private ArrayList<BasicBlock> succs;

    public BasicBlock(String blockBaseName, ArrayList <Statement> stmts, int startIndex, ArrayList<CFGVar> activesIn, 
        ArrayList<BasicBlock> preds, CFGVar tmp, ArrayList<String> locals, BasicBlock jmpBack) {
            this();
            this.setupBlock(blockBaseName, stmts, startIndex, activesIn, preds, tmp, locals, jmpBack);
        }
    
    public BasicBlock() { //placeholder constructor to just initialize arraylists
        CtrlFlowGraph.CFGCodeBlock.add(this);
        preds = new ArrayList<>();
        succs = new ArrayList<>();
        actives = new ArrayList<>();
        ops = new ArrayList<>();
        return;
    }


    private void setupBlock(String blockBaseName, ArrayList <Statement> stmts, int startIndex, ArrayList<CFGVar> activesIn, 
        ArrayList<BasicBlock> preds, CFGVar tmp, ArrayList<String> locals, BasicBlock jmpBack) {
        this.identifier = blockBaseName + (blockId > 0 ? blockId : "");
        this.preds.addAll(preds);
        blockId++;
        if(preds.size() == 0) //if this is the starting pt of the method
            this.actives.addAll(activesIn);
        else
            this.actives.addAll(preds.get(0).getActives());
        //make phis
        /*if(this.preds.size() >= 2) {
            ArrayList<BasicBlock> phiBlocks;
            ArrayList<CFGVar> phiVars;
            CFGVar latest;
            for(int i = 0; i < this.preds.get(0).actives.size(); i++) {
                phiBlocks = new ArrayList<>();
                phiVars = new ArrayList<>();
                CFGVar curr = this.preds.get(0).actives.get(i);
                phiBlocks.add(this.preds.get(0));
                phiVars.add(curr);
                latest = curr;
                for(int j = 1; j < this.preds.size(); j++) {
                    BasicBlock p = this.preds.get(j);
                    CFGVar pVer = p.getActive(curr.name());
                    if(pVer == null)
                        throw new UnsupportedOperationException("Error: variable "+curr.name()+" may not be initialized before first use");
                    phiBlocks.add(p);
                    phiVars.add(pVer);
                    latest = latest.version() < pVer.version() ? pVer : latest;
                }
                CFGVar varOut = new CFGVar(latest);
                ops.add(new CFGPhi(varOut, phiBlocks, phiVars));
            }
        }*/
        for(BasicBlock p : this.preds) {
            p.addSucc(this);
        }
        BasicBlock afterIf, ifBlk;
        CFGVar cond;
        for(int i = startIndex; i < stmts.size(); i++) {
            Statement s = stmts.get(i);
            switch (s) {
                case AssignStmt a:
                    CFGVar assignment;
                    String name = a.var().name();
                    CFGVar base = getActive(name);
                    if(base != null) {
                        assignment = base; //variable has already been initialized, we need a new version
                    } else if (locals.contains(name)) {
                        assignment = new CFGVar(name); //variable is a local that has not been initalized yet, we need to initialize it
                    } else
                        throw new IllegalArgumentException("Post-Parse error: Cannot initialize variable "+name+" as it was neither passed as an argument nor declared as a local.");
                    switch (a.rhs()) {
                        case Binop b:
                            CFGValue lhs = exprToCFG(b.lhs(), ops, actives, tmp, locals, false);
                            CFGValue rhs = exprToCFG(b.rhs(), ops, actives, tmp, locals, false);
                            ops.add(new CFGBinaryAssn(assignment, lhs, b.op(), rhs));
                            break;
                        case ThisExpr t:
                            throw new IllegalArgumentException("Error: Cannot assign 'this' to a variable value");
                        case ClassRef c:
                            CFGClass classData = CtrlFlowGraph.findClass(c.classname());
                            if (classData == null)
                                throw new IllegalArgumentException("Class " + c.classname() + " is undefined");
                            ops.add(new CFGAlloc(assignment, new CFGPrimitive(classData.numFields() + 2))); // alloc vtable, field map, fields
                            ops.add(new CFGStore(assignment, classData.vtable()));
                            tmp = new CFGVar(tmp);
                            ops.add(new CFGBinaryAssn(tmp, assignment, "&", new CFGPrimitive(1)));
                            ops.add(new CFGStore(tmp, classData.fields()));
                            break;
                        default:
                            ops.add(new CFGUnaryAssn(assignment,
                                    exprToCFG(a.rhs(), ops, activesIn, tmp, locals, false)));
                    }
                    this.actives.remove(base);
                    this.actives.add(assignment); //add var to the list of active names we allow
                    break;
                case IfElseStmt ie:
                    cond = (CFGVar)exprToCFG(ie.cond(), ops, activesIn, tmp, locals, true);
                    preds = new ArrayList<>();
                    preds.add(this);
                    afterIf = new BasicBlock();
                    ifBlk = new BasicBlock(blockBaseName, ie.body(), 0, activesIn, preds, tmp, locals, afterIf);  
                    BasicBlock elseBlk = new BasicBlock(blockBaseName, ie.elseBody(), 0, activesIn, preds, tmp, locals, afterIf);
                    preds.remove(this);
                    preds.add(ifBlk);
                    preds.add(elseBlk);
                    afterIf.setupBlock(blockBaseName, stmts, i + 1, activesIn, preds, tmp, locals, jmpBack);
                    jmp = new CFGCondOp(cond, ifBlk, elseBlk);
                    return;
                case IfOnlyStmt io:
                    cond = (CFGVar)exprToCFG(io.cond(), ops, activesIn, tmp, locals, true);
                    preds = new ArrayList<>();
                    preds.add(this);
                    afterIf = new BasicBlock();
                    ifBlk = new BasicBlock(blockBaseName, io.body(), 0, activesIn, preds, tmp, locals, afterIf);
                    preds.add(ifBlk);
                    afterIf.setupBlock(blockBaseName, stmts, i+1, activesIn, preds, tmp, locals, jmpBack);
                    jmp = new CFGCondOp(cond, ifBlk, afterIf);
                    return;
                case WhileStmt w:
                    cond = (CFGVar)exprToCFG(w.cond(), ops, actives, tmp, locals, true);
                    BasicBlock loophead = new BasicBlock();
                    preds.add(loophead);
                    loophead.addActives(actives);
                    BasicBlock body = new BasicBlock(blockBaseName, w.body(), 0, actives, preds, tmp, locals, loophead);
                    BasicBlock end = new BasicBlock(blockBaseName, w.body(), i + 1, actives, preds, tmp, locals, jmpBack);
                    preds.remove(loophead);
                    
                    preds.add(this);
                    preds.add(body);
                    loophead.setupBlock(blockBaseName, new ArrayList<>(), 0, actives, preds, tmp, locals, null); //set up block with null body to build phi
                    cond = loophead.getActive(cond.name());
                    loophead.addJump(new CFGCondOp(cond, body, end)); //add jump at end
                    jmp = new CFGAutoJumpOp(loophead);  
                        return;
                case PrintStmt p:
                    CFGValue prt = exprToCFG(p.str(), ops, activesIn, tmp, locals, false);
                    ops.add(new CFGPrint(prt));
                    break;
                case FieldWriteStmt f:
                    break;
                case ReturnStmt r:
                    jmp = new CFGRetOp(exprToCFG(r.output(), ops, activesIn, tmp, locals, false));
                    return;
                case VoidStmt v:
                    CFGValue voidRslt = exprToCFG(v.rhs(), ops, activesIn, tmp, locals, false);
                    ops.add(new CFGUnaryAssn(new CFGVar(tmp), voidRslt));
                    break;
                    default:
                    break;
            }
        }
        if(jmpBack == null)
            jmp = new CFGRetOp(new CFGPrimitive(0));
        else
            jmp = new CFGAutoJumpOp(jmpBack);
        return;
    }


    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(identifier + ":\n");
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

    //update usages (not assignments) of a variable to a new version
    private void updateUses(CFGVar v) {
        for(int i = 0; i < ops.size(); i++) {
            CFGOp o = ops.get(i);
            switch (o) {
                case CFGUnaryAssn u:
                    if(v.equals((CFGVar)u.val())) {
                        ops.add(i, new CFGUnaryAssn(u.var(), v));
                        ops.remove(o);
                    }
                    break;
                case CFGBinaryAssn b:
                    if(v.equals((CFGVar)b.lhs())) {
                        CFGBinaryAssn b1 = new CFGBinaryAssn(b.res(), v, b.op(), b.rhs());
                        ops.add(i, b1);
                        ops.remove(b);
                        b = b1;
                    }
                    if(v.equals((CFGVar)b.rhs())) {
                        CFGBinaryAssn b1 = new CFGBinaryAssn(b.res(), b.lhs(), b.op(), v);
                        ops.add(i, b1 );
                        ops.remove(b);
                        b = b1;
                    }
                default:
                    break;
            }
        }
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
    public CFGValue exprToCFG(Expression expr, ArrayList<CFGOp> ops, ArrayList<CFGVar> actives, CFGVar tmp, ArrayList<String> locals, boolean requireVar) {
        switch (expr) {
            case Constant c:
                if(requireVar) {
                    actives.remove(tmp);
                    tmp = new CFGVar(tmp);
                    actives.add(tmp);
                    ops.add(new CFGUnaryAssn(tmp, new CFGPrimitive(c.value())));
                    return tmp;
                }
                return new CFGPrimitive(c.value());
            case Variable v:
                CFGVar tmpVar = getActive(v.name());
                if(tmpVar == null)
                    throw new IllegalArgumentException("Attempted to access nonexistent or uninitialized variable "+v.name());
                return tmpVar;
            case Binop b:
                actives.remove(tmp);
                tmp = new CFGVar(tmp);
                actives.add(tmp);
                CFGValue lhs = exprToCFG(b.lhs(), ops, actives, tmp, locals, false); 
                CFGValue rhs = exprToCFG(b.rhs(), ops, actives, tmp, locals, false);
                ops.add(new CFGBinaryAssn(tmp, lhs, b.op(), rhs));
                return tmp;
            case ClassRef c: //used for class reference in a complex expression, so we need to return an anonymous(temp) value
                CFGClass classData = CtrlFlowGraph.findClass(c.classname());
                if(classData == null)
                    throw new IllegalArgumentException("Class "+c.classname()+" is undefined");
                tmp = new CFGVar(tmp);
                CFGVar cRef = tmp;
                actives.add(tmp);
                ops.add(new CFGAlloc(tmp, new CFGPrimitive(classData.numFields()+2))); //alloc vtable, field map, fields
                ops.add(new CFGStore(tmp, classData.vtable()));
                tmp = new CFGVar(tmp);
                ops.add(new CFGBinaryAssn(tmp, cRef, "&", new CFGPrimitive(8)));
                ops.add(new CFGStore(tmp, classData.fields()));
                return cRef;
            default:
                return null;
        }
    }

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
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: <comp> {tokenize|parseExpr} [args...]");
            System.exit(1);
        }
        String filePath;


        // This is just some code to kick the tires on the tokenizer, your compiler has no need to do this
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            sb.append(args[i]);
            sb.append(" "); // Without this, separate identifiers will run together
        }
        Tokenizer tok = new Tokenizer(sb.toString());
        Parser p;
        switch (args[0]) {
            case "tokenize":
                while (tok.peek().getType() != TokenType.EOF) {
                    System.out.println(tok.next());
                }
                break;
            case "parseExpr":
                p = new Parser(tok);
                System.out.println(p.parseExpr());
                break;
            case "parseStmt":
                p = new Parser(tok);
                System.out.println(p.parseStmt());
                break;
            case "parseMethod":
                p = new Parser(tok);
                System.out.println(p.parseMethod());
                break;
            case "parseClass":
                p = new Parser(tok);
                System.out.println(p.parseClass());
                break;
            case "parse":
                filePath = args[1];
                try {
                    tok = new Tokenizer(Files.readString(Path.of(filePath), StandardCharsets.UTF_8));
                } catch (Exception e) {
                    System.out.println("Failed to locate file with path "+filePath);
                    System.exit(1);
                }
                p = new Parser(tok);
                System.out.println(p.parse());
                break;
            case "mk-cfg":
                filePath = args[1];
                try {
                    tok = new Tokenizer(Files.readString(Path.of(filePath), StandardCharsets.UTF_8));
                } catch (Exception e) {
                    System.out.println("Failed to locate file with path "+filePath);
                    System.exit(1);
                }
                System.out.println(new CtrlFlowGraph(new Parser(tok).parse()));
                break;
            default:
                System.err.println("Unsupported subcommand: "+args[0]);
        }
    }
}
