public class Main { 
    public static void main(String[] args) {

	Rectangle r1 = new Rectangle();
	Rectangle r2 = new Rectangle(4.0,3.0);
	double x = r2.area();
	double y = r2.area(r1);

	RectangleWithHoles r3 = new RectangleWithHoles(7.0,4.0,2);
	x = r3.area();
	x = r3.area(r1);

	if (y > 4.0) r2 = r3;

	// Since r2 is of type Rectangle, the compile-time target of
	// r2.area() is Rectangle.area(). But since the actual object
	// could be of class RectangleWithHoles, the run-time target
	// could be the overriding method in the subclass. 
	x = r2.area();

	// since "area(Rectangle)" is not defined in the subclass,
	// Retangle.area(Rectangle) is both the compile-time and the
	// run-time target for the call below.
	x = r2.area(r1);
    }
}
