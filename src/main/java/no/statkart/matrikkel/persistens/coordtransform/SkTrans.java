package no.statkart.matrikkel.persistens.coordtransform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Klasse som kommuniserer med transformasjons-dll via et jni-kall.
 *
 * @author Roar Ingebrigtsen
 */
public class SkTrans {

    private static final Logger logger = LoggerFactory.getLogger(SkTrans.class);
    private static final String INIT_PATH = "transformation_init";

    private static boolean isOnLinux = System.getProperty("os.name").equals("Linux");
    private String transformationPath;
    private static final Filename linuxLibrary = new Filename("libsositrans", "so");
    private static final Filename windowsLibrary = new Filename("SosiTransformasjon", "dll");
    private static Filename[] libraries = new Filename[]{
            new Filename("libifcoremd", "dll"),
            new Filename("libmmd", "dll"),
            new Filename("libSosiTransformasjon", "a"),
            linuxLibrary,
            new Filename("skt2lan1_64", "dll"),
            windowsLibrary,
            new Filename("svml_dispmd", "dll")
    };
    private static Filename[] initFiles = new Filename[]{
            new Filename("arcgp-2006-sk", "bin"),
            new Filename("HREF2016B_NN2000_EUREF89", "bin"),
            new Filename("lan1_fellesp_20081014", "bin"),
            new Filename("Milne_east", "bin"),
            new Filename("NNTrans2016B", "bin"),
            new Filename("Uplift_Svalbard_pdim_pgs_tot", "dat"),
            new Filename("href2008a", "bin"),
            new Filename("IGS05N_EUREF89_7PAR_2013", "txt"),
            new Filename("lan1_fellesp", "bin"),
            new Filename("Milne_north", "bin"),
            new Filename("RH2000LU_absup", "bin")
    };

    private native int xSosiTrans(int fraKoordSys, double fraX, double fraY, double fraH, int tilKoordSys, double[] returTall);
    private native boolean initialize(String init_path);
    private native String getLastError();

    SkTrans() {
        loadDll();
    }

    private void copyFile(String dir, Filename filename, Path destination) {
        String srcPath = "/" + dir + "/" + filename.name();
        InputStream istream = SkTrans.class.getResourceAsStream(srcPath);
        if (istream == null) {
            throw new RuntimeException(String.format("Failed to get input stream from %s in %s",
                    srcPath, this.getClass().getResource("SkTrans.class")));
        }
        try {
            Path target = Paths.get(destination.toString(), filename.name());
            Files.copy(istream, target, StandardCopyOption.REPLACE_EXISTING);
            filename.setInstalledPath(target.toFile());
            registerFileForCleanup(new File(filename.getInstalledPath().toString()));
        } catch (IOException e) {
            logger.error("Failed to copy file {} from {} to {}", filename.name(), dir, destination.toString(), e);
            logger.error("istream: {}", istream);
            logger.error("srcPath: {}", srcPath);
            logger.error("destination: {}", destination);
            throw new RuntimeException(e);
        }
    }

    private void copyFiles(Path tmpDir) {
        Path initDir = Paths.get(tmpDir.toString(), "transformation_init");
        try {
            initDir = Files.createDirectory(initDir);
            registerFileForCleanup(initDir.toFile());
        } catch (IOException e) {
            logger.error("Failed to create temporary directory structure", e);
            throw new RuntimeException(e);
        }
        for (Filename lib: libraries) {
            copyFile("lib", lib, tmpDir);
        }
        for (Filename initFile: initFiles) {
            copyFile("transformation_init", initFile, initDir);
        }
    }

    private void registerFileForCleanup(File file) {
        logger.debug("Registering {} for cleanup", file.toString());
        file.deleteOnExit();
    }

    private void setupNativeFiles() {
        Path tmpdir = null;
        try {
            tmpdir = Files.createTempDirectory("sktrans-lib");
            registerFileForCleanup(tmpdir.toFile());
            logger.info("Created temporary directory {}", tmpdir.getFileName());
        } catch (IOException e) {
            logger.error("Failed to create temporary directory", e);
            throw new RuntimeException(e);
        }
        try {
            copyFiles(tmpdir);
        } catch (RuntimeException e) {
            logger.warn("Attempting to remove temporary directory {} since exception '{}' was thrown",
                    tmpdir.getFileName(), e.getMessage());
            //noinspection ResultOfMethodCallIgnored
            tmpdir.toFile().delete();
            throw e;
        }
    }

    private static class Filename {
        private final String prefix;
        private final String suffix;
        private File installedPath;

        Filename(String prefix, String suffix) {
            this.prefix = prefix;
            this.suffix = suffix;
        }

        String name() {
            return prefix + "." + suffix;
        }

        void setInstalledPath(File path) {
            installedPath = path;
        }

        File getInstalledPath() {
            return installedPath;
        }
    }

    private void loadDll() {
        setupNativeFiles();
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

    private void xSosiTransWrapper(int fraKoordSys, double fraX, double fraY, double fraH, int tilKoordSys, double[] returTall) {
        int res = xSosiTrans(fraKoordSys, fraX, fraY, fraH, tilKoordSys, returTall);
        logger.debug(fraKoordSys + ": (" + fraX + ", " + fraY + ") -> " + tilKoordSys + ": (" + returTall[0] + ", " + returTall[1] + ") [" + res + "]");
        if (SkTransException.isError(res)) {
            throw new SkTransException(SkTransException.ErrorCode.fromInt(res));
        }
    }

    /**
     * Kaller dll via JNI for å utføre transformasjon av koordinater.
     *
     * @param y             y-koordinat som skal transformeres
     * @param x             x-koordinat som skal transformeres
     * @param z             z-koordinat som skal transformeres
     * @param sosiFraSystem sosi-kode for koordinatsystem som y, x og z
     * @param sosiTilSystem sosi-kode for koordinatsystem som koordinatene skal transformeres til
     * @return et array med de transformerte koordinatene; [y, x, z]
     */
    public synchronized double[] transform(double y, double x, double z, int sosiFraSystem, int sosiTilSystem) {
        short fraSosiSys = (short) sosiFraSystem;
        short tilSosiSys = (short) sosiTilSystem;
        double[] transformert = new double[3];

        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new RuntimeException("Transformasjon kalles med ugyldige tall som transformasjonsverdier! x: " + x + ", y: " + y + ", z: " + z);
        }

        synchronized (SkTrans.class) {
            //I native biblioteket(skTrans) forventes det at koordinatene er i Nord Øst format, derfor kalles den med y, x, z
            //noinspection SuspiciousNameCombination
            xSosiTransWrapper(fraSosiSys, y, x, z, tilSosiSys, transformert);
        }
        return transformert;
    }

    /**
     * Kaller dll via JNI for å utføre transformasjon et array av koordinater
     *
     * @param in            array med koordinater som skal transtransformeres: [x1,y1,z1,...,xN,yN,zN] eller [x1,y1...,xN,yN]
     * @param dimensions    angir om array inneholder 2 eller 3 verdier per koordinat
     * @param sosiFraSystem sosi-kode for koordinatsystem som x, y og z
     * @param sosiTilSystem sosi-kode for koordinatsystem som koordinatene skal transformeres til
     * @return et array med de transformerte koordinatene: [x1,y1,z1,...,xN,yN,zN] eller [x1,y1...,xN,yN]
     */
    public double[] transform(double[] in, final int dimensions, int sosiFraSystem, int sosiTilSystem) {
        if (sosiFraSystem == sosiTilSystem) {
            return in;
        } else {
            short fraSosiSys = (short) sosiFraSystem;
            short tilSosiSys = (short) sosiTilSystem;
            double[] out = new double[in.length];
            double[] transformert = new double[3];
            synchronized (SkTrans.class) {
                for (int i = 0; i < in.length; i += dimensions) {
                    // Note: rekkefølgen er y,x,z
                    xSosiTransWrapper(fraSosiSys, in[i + 1], in[i], (dimensions == 3) ? in[i + 2] : 0d, tilSosiSys, transformert);
                    out[i] = transformert[1];
                    out[i + 1] = transformert[0];
                    if (dimensions == 3) {
                        out[i + 2] = transformert[2];
                    }
                }
            }
            return out;
        }
    }
}
