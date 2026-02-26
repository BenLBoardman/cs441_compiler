package util;

public enum CompilePhase {
    TOKEN,
    PARSE,
    CFG,
    SSA_1,
    OPT_VN,
    DCE;

    @Override
    public String toString() {
        switch (this) {
            case TOKEN:
                return "tokenize";
            case PARSE:
                return "parse";
            case CFG:
                return "initial CFG construction";
            case SSA_1:
                return "initial SSA";
            case OPT_VN:
                return "value numbering optimization";
            case DCE:
                return "dead code elimination";
            default:
                return "";
        }
    }
}
