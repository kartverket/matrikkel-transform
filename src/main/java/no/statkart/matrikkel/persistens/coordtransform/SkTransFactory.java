package no.statkart.matrikkel.persistens.coordtransform;

public class SkTransFactory {
    private static SkTrans instance;
    public static SkTrans getInstance() {
        synchronized (SkTransFactory.class) {
            if (instance == null) {
                instance = new SkTrans();
            }
            return instance;
        }
    }
}
