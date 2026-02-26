package cfg.expr.data;

public record CFGArray(String name, Object[] elems) implements CFGData { public int size()  {return this.elems.length; } @Override public String toString() { return "@"+name;} }

