public class RectangleWithHoles extends Rectangle { 

    private int hole_size;

    public RectangleWithHoles(double height, double width, int hole_size) {
	super(height,width);
	this.hole_size = hole_size;
    }

    // overrides "double area()" in the superclass
    public double area() { 
	return super.area() - hole_size;
    }

}
