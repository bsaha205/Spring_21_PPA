public class A {
    X[][] ax = new X[1][1];
    X f = null;
    
    void set() {
	X x = new Y();
	ax[0][0] = x;
	f = x;
    }
    X get() {
	return ax[0][0];
    }

    void n() { 
	X x = this.f;
    }

    public static void main(String[] args) {
	A a = new A();
	a.set();
	X x = a.get();
	x.m();
    }
}

class B extends A {
    void n() {
	X x = new Z();
    }
} 

class X {
    X m() { 
	return null; 
    }
}

class Y extends X {
    X m() { 
	A a = new B(); 
	return new X(); 
    }
}

class Z extends Y {}