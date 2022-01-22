
//public class A {
//    Cloneable o;
//
//    public static void main(String[] args) {
//	A a = new A();
//	a.m();
//
//    }
//    public void m() {
//    }
//}


// the original P1
//public class A {
//
//    public static void main(String[] args) {
//		n1();
//		n2();
//    }
//    
//    static void n1() {
//    	A a1 = new B();
//    	a1.m();
//    }
//    
//    static void n2() {
//    	A a2 = new C();
//    	a2.m();
//    }
//    
//    void m() {
//    	
//    }
//}
//
//class B extends A {
//	void m() {
//		
//	}
//} 
//
//class C extends A {
//	
//} 



abstract class BooleanExp {
	abstract boolean evaluate();
}

abstract class CompositeExp extends BooleanExp {
	BooleanExp left;
	BooleanExp right;

	void init(BooleanExp left, BooleanExp right) {
		this.left = left;
		this.right = right;
	}

	abstract boolean evaluate();
}

class ConstExp extends BooleanExp {
	boolean value;

	ConstExp(boolean value) {
		this.value = value;
	}

	boolean evaluate() {
		return this.value;
	}
}

class VarExp extends BooleanExp {
	String name;

	VarExp(String name) {
		this.name = name;
	}

	boolean evaluate() {
		return Context.lookup(name);
//		return true;
	}
}

class OrExp extends CompositeExp {
	void init(BooleanExp left, BooleanExp right) {
		super.init(left, right);
	}

	boolean evaluate() {
		BooleanExp l = this.left;
		BooleanExp r = this.right;
		return l.evaluate() || r.evaluate();
	}
}

class AndExp extends CompositeExp {
	void init(BooleanExp left, BooleanExp right) {
		super.init(left, right);
	}

	boolean evaluate() {
		BooleanExp l = this.left;
		BooleanExp r = this.right;
		return l.evaluate() && r.evaluate();
	}
}

class Context {
    void assign(Boolean varExp, boolean b) {
    }

    static boolean lookup(String var) {
    	return true;
    }
}

public class A{
	public static void main(String[] args) {
		//... assigns context
		Context theContext = new Context();
		BooleanExp x = new VarExp("X"); //o1
		BooleanExp y = new VarExp("Y"); //o2
		BooleanExp c = new ConstExp(true); //o3
		CompositeExp o = new OrExp(); //o4
		o.init(x,y);
		CompositeExp exp = new AndExp(); //o5
		exp.init(c,o);
		BooleanExp z = exp.left;
		boolean b = exp.evaluate();
	}
}
