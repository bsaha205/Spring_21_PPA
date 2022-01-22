class A {
    public static void main(String[] argc) {
	B b = new B();
	D d = new D();
	A a;
	if (argc[1].equals("ana")) a = d;
	else a = b;
	a.m(); 
    }
    public void m() { }
}
class B extends A {
    public void n() { C c = new C(); }
    public void m() { G g = new G(); }
}
class G extends B {
    public void m() { A a = new A(); }
}
class C extends A {
    public void m() { }
}
class D extends C { /* no definition of m */}