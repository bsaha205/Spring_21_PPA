package pack3;

public abstract class A implements pack2.X {
    public abstract void m();
    private int x = 0;
    public void m2() { 
	if (x > 1) return;
	x++;
	m(); 
    }
}

