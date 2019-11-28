package no.statkart.matrikkel.persistens.coordtransform;

public class SkTransFactory {
    private static SkTrans instance;
    public static SkTrans getInstance() {
        if (instance == null) {
            instance = new SkTrans();
        }
        return instance;
    }
}
