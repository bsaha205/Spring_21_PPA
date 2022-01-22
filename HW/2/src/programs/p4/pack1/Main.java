package pack1;

import pack2.*;
import pack3.*;

public class Main { 

    public static void main(String[] args) {

	B b = new B();
	b.m();
	b.m2();
	b.n();
	
	// C c = new C();
	B c = new B();
	sm1(c);
	sm2(b);
	sm3(c);
    }
    
    public static void sm1(A a) {
	a.m();
	a.m2();
    }

    public static void sm2(X x) { x.m(); }
    
    public static void sm3(Y y) { y.n(); }
}
