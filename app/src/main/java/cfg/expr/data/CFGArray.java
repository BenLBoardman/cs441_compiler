package cfg.expr.data;

public class CFGArray extends CFGData {
    private String name;
    private Object[] elems;

    public CFGArray(String name, Object[] elems) {
        this.name = name;
        this.elems = elems;
    }

    public int size() {
        return this.elems.length;
    }

    public String name() {
        return name;
    }

    public Object[] elems() {
        return elems;
    }

    @Override
    public String toString() {
        return "@" + name;
    }
}
