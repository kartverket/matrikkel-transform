package no.statkart.matrikkel.persistens.coordtransform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Klasse som kommuniserer med transformasjonsbiblioteket via et jni-kall.
 */
public class SkTrans {
    private static final Logger logger = LoggerFactory.getLogger(SkTrans.class);
    private static final String INSTALL_DIRECTORY_INFIX = "statkart/matrikkelen";
    private static boolean isOnLinux = System.getProperty("os.name").equals("Linux");
    private static final String DIGEST_ALGORITHM = "SHA-256";
    private static final String TRANSFORMATION_INIT_DIR_NAME = "transformation_init";
    private static final String LIB_DIR_NAME = "lib";
    private static Filename linuxLibrary = new Filename("libsositrans.so");
    private static Filename windowsLibrary = new Filename("SosiTransformasjon.dll");
    private static Filename[] libraries = {
            linuxLibrary,
            windowsLibrary,
            new Filename("libifcoremd.dll"),
            new Filename("libmmd.dll"),
            new Filename("libSosiTransformasjon.a"),
            new Filename("skt2lan1_64.dll"),
            new Filename("svml_dispmd.dll"),
    };
    private static Filename[] initFiles = {
            new Filename("arcgp-2006-sk.bin"),
            new Filename("HREF2016B_NN2000_EUREF89.bin"),
            new Filename("lan1_fellesp_20081014.bin"),
            new Filename("Milne_east.bin"),
            new Filename("NNTrans2016B.bin"),
            new Filename("Uplift_Svalbard_pdim_pgs_tot.dat"),
            new Filename("href2008a.bin"),
            new Filename("IGS05N_EUREF89_7PAR_2013.txt"),
            new Filename("lan1_fellesp.bin"),
            new Filename("Milne_north.bin"),
            new Filename("RH2000LU_absup.bin"),
    };

    /**
     * EUREFGeo opererer i sekunder, mens vi i vår kode og i databasen er interessert i grader.
     * Vi må derfor gange opp før transformering og så dele ned igjen etterpå
     */
    private static final int SECONDS = 3600;
    private static final int EUREFGEO = 84;

    private boolean ignoreZ = false;

    public static SkTrans zIgnoringInstance() {
        SkTrans transformer = new SkTrans();
        transformer.ignoreZ = true;
        return transformer;
    }

    private native int xSosiTrans(int fraKoordSys, double nord, double ost, double hoyde, int tilKoordSys, double[] returTall);
    private native boolean initialize(String init_path);
    private native String getLastError();

    SkTrans() {
        setupAndLoadLibrary();
        initializeLibrary();
    }

    private static void copyFile(String dir, Filename filename, Path destination) {
        String srcPath = '/' + dir + '/' + filename.name();
        try (InputStream istream = SkTrans.class.getResourceAsStream(srcPath)) {
            if (istream == null) {
                throw new RuntimeException(String.format("Failed to get input stream from %s in %s",
                        srcPath, SkTrans.class.getResource("SkTrans.class")));
            }
            Path target = Paths.get(destination.toString(), filename.name());
            Files.copy(istream, target, StandardCopyOption.REPLACE_EXISTING);
            filename.setInstalledPath(target.toFile());
        } catch (IOException e) {
            logger.error("Failed to copy file {} from {} to {}", filename.name(), dir, destination.toString(), e);
            logger.error("srcPath: {}", srcPath);
            logger.error("destination: {}", destination);
            throw new RuntimeException(e);
        }
    }

    private void copyFiles(Path tmpDir) {
        Path initDir = Paths.get(tmpDir.toString(), TRANSFORMATION_INIT_DIR_NAME);
        try {
            Files.createDirectory(initDir);
        } catch (IOException e) {
            logger.error("Failed to create temporary directory structure", e);
            throw new RuntimeException(e);
        }
        for (Filename lib: libraries) {
            copyFile(LIB_DIR_NAME, lib, tmpDir);
        }
        for (Filename initFile: initFiles) {
            copyFile(TRANSFORMATION_INIT_DIR_NAME, initFile, initDir);
        }
    }

    private void loadLibraryOrCroak() {
        String libPath = getTransformationLibrary().getInstalledPath().toString();
        try {
            System.load(libPath);
        } catch (UnsatisfiedLinkError e) {
            String msg = "Lasting av " + libPath + " på server feilet:  " + e.getMessage();
            logger.error(msg);
            throw new RuntimeException(msg, e);
        } catch (Throwable e) {
            String msg = "Lasting av " + libPath + " på server feilet:  " + e.getMessage();
            throw new RuntimeException(msg, e);
        }
    }

    private boolean tryLoadLibrary() {
        String libPath = getTransformationLibrary().getInstalledPath().toString();
        try {
            System.load(libPath);
            return true;
        } catch (UnsatisfiedLinkError e) {
            return false;
        }
    }

    private void setupAndLoadLibrary() {
        String libraryPathName = System.getProperty("no.statkart.matrikkel.transform.library.path");
        if (libraryPathName == null) {
            libraryPathName = System.getProperty("java.io.tmpdir");
            if (libraryPathName == null) {
                throw new RuntimeException("Neither java.io.tmpdir nor no.statkart.matrikkel.transform.library.path is set, cannot continue");
            }
            int counter = 1;
            while (true) {
                Path libraryPath = Paths.get(libraryPathName, INSTALL_DIRECTORY_INFIX, String.valueOf(counter));
                if (Files.exists(libraryPath)) {
                    if (testPath(libraryPath) && tryLoadLibrary()) {
                        return;
                    }
                    counter++;
                } else {
                    buildPath(libraryPath);
                    loadLibraryOrCroak();
                    return;
                }
            }
        }
        throw new RuntimeException("Reached unreachable state");
    }

    private boolean testPath(Path path) {
        for (Filename lib: libraries) {
            if (testFile(path.toString(), lib, LIB_DIR_NAME)) {
                return false;
            }
        }
        for (Filename initFile: initFiles) {
            if (testFile(Paths.get(path.toString(), TRANSFORMATION_INIT_DIR_NAME).toString(), initFile, TRANSFORMATION_INIT_DIR_NAME)) {
                return false;
            }
        }
        logger.info("Library path {} matches package, reusing existing", path);
        return true;
    }

    private static boolean testFile(String filesystemDir, Filename filename, String jarDir) {
        File fileSystemPath = Paths.get(filesystemDir, filename.name()).toFile();
        if (!fileSystemPath.exists()) {
            logger.debug("Couldn't find filesystem path for {}", fileSystemPath);
            return true;
        }
        filename.setInstalledPath(fileSystemPath);
        String jarPath = '/' + jarDir + '/' + filename.name();
        InputStream jarStream = SkTrans.class.getResourceAsStream(jarPath);
        if (jarStream == null) {
            throw new RuntimeException(String.format("Failed to get input stream from %s in %s",
                    jarPath, SkTrans.class.getResource("SkTrans.class")));
        }

        try (DigestInputStream jarDis = new DigestInputStream(jarStream, MessageDigest.getInstance(DIGEST_ALGORITHM));
             DigestInputStream fileDis = new DigestInputStream(new FileInputStream(fileSystemPath), MessageDigest.getInstance(DIGEST_ALGORITHM))) {
            byte[] fileDigest = fileDis.getMessageDigest().digest();
            byte[] jarDigest = jarDis.getMessageDigest().digest();
            if (Arrays.equals(fileDigest, jarDigest)) {
                return false;
            }
            logger.debug("Filesystem file {} ({}) does not match jar file {} ({})", fileSystemPath, fileDigest, jarPath, jarDigest);
            return true;
        } catch (IOException e) {
            logger.error("Failed to compare files {} and {}: {}", fileSystemPath.toString(), jarPath, jarDir, e);
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void buildPath(Path path) {
        try {
            Files.createDirectories(path);
            logger.info("Created SkTrans directory {}", path.getFileName());
        } catch (IOException e) {
            logger.error("Failed to create directory {}", path.toString(), e);
            throw new RuntimeException(e);
        }
        try {
            copyFiles(path);
        } catch (RuntimeException e) {
            logger.warn("Attempting to remove directory {} since exception '{}' was thrown",
                    path.getFileName(), e.getMessage());
            //noinspection ResultOfMethodCallIgnored
            path.toFile().delete();
            throw e;
        }
    }

    private static class Filename {
        private final String name;
        private File installedPath;

        Filename(String name) {
            this.name = name;
        }

        String name() {
            return name;
        }

        void setInstalledPath(File path) {
            installedPath = path;
        }

        File getInstalledPath() {
            return installedPath;
        }
    }

    private void initializeLibrary() {
        String libPath = getTransformationLibrary().getInstalledPath().toString();
        logger.debug("Lasting ok, initialiserer " + libPath);
        System.err.println("Lasting ok, initialiserer " + libPath);

        if (!initialize(initFilePath())) {
            String msg = "Kunne ikke initialisere SkTrans-bibliotek: " + getLastError();
            logger.error(msg);
            throw new RuntimeException(msg);
        }
        System.err.println("Initialisering ok");
    }

    private Filename getTransformationLibrary() {
        return isOnLinux ? linuxLibrary : windowsLibrary;
    }

    private String initFilePath() {
        Path attempt = initFiles[0].getInstalledPath().toPath().getParent();
        if (!Files.exists(attempt)) {
            throw new RuntimeException("Transformation library initialization failed, couldn't resolve init path");
        }
        return attempt.toAbsolutePath().toString();
    }

    private Coordinate xSosiTransWrapper(int fraKoordSys, double oest, double nord, double fraH, int tilKoordSys) {
        int res;
        double[] returTall = new double[3];
        if (fraKoordSys == EUREFGEO) {
            nord *= SECONDS;
            oest *= SECONDS;
        }
        if (ignoreZ) {
            double z = 0;
            res = xSosiTrans(fraKoordSys, nord, oest, z, tilKoordSys, returTall);
            returTall[2] = fraH;
        } else {
            res = xSosiTrans(fraKoordSys, nord, oest, fraH, tilKoordSys, returTall);
        }
        if (logger.isDebugEnabled()) {
            logger.debug(fraKoordSys + ": (" + oest + ", " + nord + ") -> " + tilKoordSys + ": (" + returTall[1] + ", " + returTall[0] + ") [" + res + "]");
        }
        if (SkTransException.isError(res)) {
            throw new SkTransException(SkTransException.ErrorCode.fromInt(res), fraKoordSys, tilKoordSys, oest, nord, fraH);
        }
        if (tilKoordSys == EUREFGEO) {
            returTall[0] /= SECONDS;
            returTall[1] /= SECONDS;
        }
        return new Coordinate(returTall[1], returTall[0], returTall[2]);
    }

    /**
     * Kaller dll via JNI for å utføre transformasjon av koordinater.
     *
     * @param x             x-koordinat som skal transformeres
     * @param y             y-koordinat som skal transformeres
     * @param z             z-koordinat som skal transformeres
     * @param sosiFraSystem sosi-kode for koordinatsystem som y, x og z
     * @param sosiTilSystem sosi-kode for koordinatsystem som koordinatene skal transformeres til
     * @return et array med de transformerte koordinatene; [y, x, z]
     */
    public synchronized Coordinate transform(double x, double y, double z, int sosiFraSystem, int sosiTilSystem) {
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new RuntimeException("Transformasjon kalles med ugyldige tall som transformasjonsverdier! x: " + x + ", y: " + y + ", z: " + z);
        }

        synchronized (SkTrans.class) {
            return xSosiTransWrapper(sosiFraSystem, x, y, z, sosiTilSystem);
        }
    }

    /**
     * Kaller dll via JNI for å utføre transformasjon et array av koordinater
     *
     * @param in            array med koordinater som skal transformeres: [x1,y1,z1,...,xN,yN,zN] eller [x1,y1...,xN,yN]
     * @param dimensions    angir om array inneholder 2 eller 3 verdier per koordinat
     * @param sosiFraSystem sosi-kode for koordinatsystem som x, y og z
     * @param sosiTilSystem sosi-kode for koordinatsystem som koordinatene skal transformeres til
     * @return et array med de transformerte koordinatene: [x1,y1,z1,...,xN,yN,zN] eller [x1,y1...,xN,yN]
     */
    public Coordinate[] transform(double[] in, final int dimensions, int sosiFraSystem, int sosiTilSystem) {
        Coordinate[] result = new Coordinate[in.length / dimensions];
        int resultIndex = 0;
        for (int i = 0; i < in.length; i += dimensions) {
            if (sosiFraSystem == sosiTilSystem) {
                result[resultIndex++] = new Coordinate(in[i], in[i + 1], dimensions == 3 ? in[i + 2] : Double.NaN);
            } else {
                synchronized (SkTrans.class) {
                    result[resultIndex++] = xSosiTransWrapper(sosiFraSystem, in[i], in[i + 1], (dimensions == 3) ? in[i + 2] : Double.NaN, sosiTilSystem);
                }
            }
        }
        return result;
    }
}
