class B extends A {
}

public class A {

    private B f;
    private int x;
    private static double y;

    public static void main(String[] args) {
	sm();
	y = 5.6;
    }

    public static void sm() {
	A a = new A();
	a.m();
	a.f = new B();
    }
    public void m() {
	x = 3;
	A b = new A();
	B c = this.f;
	int y = c.add(b);
	c = this.f;
    }
    public int add(A z) {
	return this.x + z.x;
    }
}

