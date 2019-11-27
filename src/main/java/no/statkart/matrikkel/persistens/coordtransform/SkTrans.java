package no.statkart.matrikkel.persistens.coordtransform;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Klasse som kommuniserer med transformasjons-dll via et jni-kall.
 *
 * @author Roar Ingebrigtsen
 */
public class SkTrans {

   private SkTransLogger logger;
   private static final String INIT_PATH = "transformation_init";

   private static SkTrans instance;
   private static boolean isOnLinux = System.getProperty("os.name").equals("Linux");
   private String transformationPath;

   private native int xSosiTrans(int fraKoordSys, double fraX, double fraY, double fraH, int tilKoordSys, double[] returTall);
   private native boolean initialize(String init_path);
   private native String getLastError();

   private SkTrans(String transformationPath, SkTransLogger logger) {
      this.transformationPath = transformationPath;
      this.logger = logger;
   }

   public static synchronized void createInstance(String transformationPath, SkTransLogger logger) {
      assert instance == null;
      instance = new SkTrans(transformationPath, logger);
      instance.loadDll();
   }

   /**
    * Denne skal kun kalles av SkTransFactory, som først setter opp instance vha. {@link #createInstance}
    * @return SkTrans singleton instance
    */
   public static SkTrans getInstance() {
      return instance;
   }

   public static boolean hasInstance() {
      return instance != null;
   }

   private void loadDll() {
      String libraryFilePath = Paths.get(transformationPath, getTransformationLibraryName()).toString();
      try {
         System.load(libraryFilePath);
      } catch (UnsatisfiedLinkError e) {
         String msg = "Lasting av " + libraryFilePath + " på server feilet:  " + e.getMessage();
         logger.error(msg);
         throw new RuntimeException(msg, e);
      } catch (Throwable e) {
         String msg = "Lasting av " + libraryFilePath + " på server feilet:  " + e.getMessage();
         throw new RuntimeException(msg, e);
      }
      logger.debug("Lasting ok, initialiserer " + libraryFilePath);
      System.err.println("Lasting ok, initialiserer " + libraryFilePath);

      if (!initialize(initFilePath())) {
         String msg = "Kunne ikke initialisere SkTrans-bibliotek: " + getLastError();
         logger.error(msg);
         throw new RuntimeException(msg);
      }
      System.err.println("Initialisering ok");
   }
   private String getTransformationLibraryName() {
      return isOnLinux ? "libsositrans.so" : "SosiTransformasjon.dll";
   }

   private String initFilePath() {
      Path attempt = Paths.get(transformationPath, INIT_PATH).toAbsolutePath();
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
