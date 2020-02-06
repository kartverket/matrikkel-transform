package no.statkart.matrikkel.persistens.coordtransform;

public class SkTransFactory {
    private static SkTrans instance;
    private static SkTrans zIgnoringInstance;

    /**
     * Create or return a singleton SkTrans instance that transforms (x, y, z) coordinates
     * between coordinate systems
     *
     * The returned instance is separate from the instance returned by {@link #getZIgnoringInstance()}
     * @return An SkTrans instance
     */
    public static SkTrans getInstance() {
        synchronized (SkTransFactory.class) {
            if (instance == null) {
                instance = new SkTrans();
            }
            return instance;
        }
    }

    /**
     * Create or return a singleton SkTrans instance that transforms (x, y) coordinates
     * between coordinate systems and ignores z coordinates. This means that z coordinates
     * that are passed into transformation methods are silently returned as resulting z
     * coordinates without actually being transformed by the underlying library.
     *
     * The returned instance is separate from the instance returned by {@link #getInstance()}
     * @return An SkTrans instance ignoring z coordinates
     */
    public static SkTrans getZIgnoringInstance() {
        synchronized (SkTransFactory.class) {
            if (zIgnoringInstance == null) {
                zIgnoringInstance = SkTrans.zIgnoringInstance();
            }
            return zIgnoringInstance;
        }
    }
}
