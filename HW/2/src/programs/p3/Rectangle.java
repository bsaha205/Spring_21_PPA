public class Rectangle { 

    private double height, width;

    public Rectangle(double height, double width) {
	this.height = height; this.width = width;
    }

    public Rectangle() {
	this(0.0,0.0);
    }

    public double area() { 
	return height * width; 
    }

    public double area(Rectangle r) {
	return (height - r.height) * (width - r.width);
    }

    
}
