package multimediaquery;

public class Complex {

    private final double real; // the real part
    private final double imag; // the imaginary part

    public Complex(double real, double imag) {
        this.real = real;
        this.imag = imag;
    }

    public Complex plus(Complex other) {
        double real = this.real + other.real;
        double imag = this.imag + other.imag;
        return new Complex(real, imag);
    }

    public Complex minus(Complex other) {
        double real = this.real - other.real;
        double imag = this.imag - other.imag;
        return new Complex(real, imag);
    }

    public Complex times(Complex other) {
        double real = this.real * other.real - this.imag * other.imag;
        double imag = this.real * other.imag + this.imag * other.real;
        return new Complex(real, imag);
    }

    // return the real part
    public double real_part() {
        return this.real;
    }

    //return imagin part
    public double imagin_part() {
        return this.imag;
    }
}
