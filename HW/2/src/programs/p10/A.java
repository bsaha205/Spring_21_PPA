abstract class BoolExp {
    public abstract boolean evaluate(Context c); 
}

class Constant extends BoolExp {
    private boolean constant;

    public Constant(boolean value) { this.constant = value; }
    public boolean evaluate(Context c) {
	return constant; 
    }
}

class VarExp extends BoolExp {
    private String name;

    public VarExp(String name) { this.name = name; }
    public boolean evaluate(Context c) {
        return c.lookup(name);
    }
    public String getName() { return name; }
}

class AndExp extends BoolExp {
    private BoolExp left;
    private BoolExp right;

    public AndExp(BoolExp left, BoolExp right) { 
	this.left = left;   
	this.right = right; 
    }
    public boolean evaluate(Context c) {
	return left.evaluate(c) && right.evaluate(c); 
    }
}

class OrExp extends BoolExp {
    private BoolExp left;
    private BoolExp right;

    public OrExp(BoolExp left, BoolExp right) { 
	this.left = left;
	this.right = right; 
    }
    public boolean evaluate(Context c) {
	return left.evaluate(c) || right.evaluate(c); 
    }
}

class Context {
    void assign(BoolExp varExp, boolean b) {
    }

    boolean lookup(String var) {
	return true;
    }
}

public class A {
    public static void main(String[] args) {
	Context theContext = new Context();
	BoolExp x = new VarExp("X");
	BoolExp y = new VarExp("Y");
	BoolExp exp = new AndExp(new Constant(true), new OrExp(x, y) );
	theContext.assign(x, true);
	theContext.assign(y, false);
	boolean result = exp.evaluate(theContext);
    }

}

