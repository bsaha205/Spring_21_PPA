public class A {

    public static void main(String[] args) {

	B b = new B();
	b.m();
	
	A a = b.n();
	a.m();

	C.sm();
    }

    public void m() {}
    public A n() { return this; }
}

