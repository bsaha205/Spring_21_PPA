public class A {

    public static void main(String[] args) {

	A a = B.fld;
	a.m();
    }

    void m() {
	C c = new C();
	n(c);
    }

    void n(B b) {
	b.m();
    }
}
