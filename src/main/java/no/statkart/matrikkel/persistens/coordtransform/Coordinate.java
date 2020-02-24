package no.statkart.matrikkel.persistens.coordtransform;

/**
 * Hjelpeklasse for å lagre et koordinat.
 */
public class Coordinate {
    final private double oest;
    final private double nord;
    final private double hoyde;

    public Coordinate(double oest, double nord, double hoyde) {
        this.oest = oest;
        this.nord = nord;
        this.hoyde = hoyde;
    }

    public double getOest() {
        return oest;
    }

    public double getNord() {
        return nord;
    }

    public double getHoyde() {
        return hoyde;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Coordinate)) {
            return false;
        }
        Coordinate other = (Coordinate) obj;
        return other.nord == nord && other.oest == oest && other.hoyde == hoyde;
    }

    @Override
    public String toString() {
        return String.format("Coordinate(%f, %f, %f)", oest, nord, hoyde);
    }
}
