package tokenize;

public class Tokenizer {

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
    private final Returning returning = new Returning();
    private final Int intTok = new Int();
    private final NullTok nullTok = new NullTok();

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
                    return new NumberTok(Integer.parseInt(text.substring(start,current)));
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
                        case "returning": return returning;
                        case "int": return intTok;
                        case "null": return nullTok;
                        default: return new Identifier(fragment);
                    }
                } else {
                    throw new IllegalArgumentException("Unsupported character: "+text.charAt(current));
                }
        }
    }
}
