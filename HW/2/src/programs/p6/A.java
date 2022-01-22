public class A {

    private int x = 1;
    private String s = "initial";

    // in the bytecode, all initializations of static fields are put
    // in a "fake" static method <clinit>. This methods is executed
    // once, when the JVM loads the class. 
    public static int y = 2;
    public static A a = null;

    public static int p, q;
    // static initialization block: executed once when the class is
    // loaded in the JVM. Quite similar to a field initializer for a
    // static field. The code in static initializers is included in
    // <clinit>.
    static { p = 5; y = y + p + 2; }

    public A() {}
    public A(int x) {}

    // a "finalize" method is executed automatically by the JVM when
    // the object is garbage-collected. quite similar to a destructor
    // call in C++. Typically this is called implicitly by the JVM;
    // the only reason to have an explicit call to a finalizer is when
    // within a finalizer we have "super.finalize()".  Even though
    // there are no explicit calls to finalizers, such methods should
    // be included in the call graph.
    protected void finalize() { s = null; }

    public static void main(String[] args) {
	A a = new A();
	A b = new A(5);
    }

    
}

