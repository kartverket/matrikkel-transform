import java.io.IOException;

class Tester {
    private native int xSosiTrans(int fraKoordSys, double fraX, double fraY, double fraH, int tilKoordSys, double[] returTall);
    private native boolean initialize(String init_path);
    private native String getLastError();

    private String library;
    private String initPath;
    private Double x;
    private Double y;
    private Integer fromSosi;
    private Integer toSosi;

    public void setToSosi(int toSosi) {
        this.toSosi = toSosi;
    }

    public void setFromSosi(int fromSosi) {
        this.fromSosi = fromSosi;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setLibrary(String library) {
        this.library = library;
    }

    public void setInitPath(String initPath) {
        this.initPath = initPath;
    }

    void fail(String value, String argument) {
        System.err.println(value + " was not provided. Do so with the " + argument + " argument");
        System.exit(1);
    }

    void verifyAllParameters() {
        if (library == null) {
            fail("Library to load (dll)", "-l");
        }
        if (initPath == null) {
            fail("Path to initialization files", "-i");
        }
        if (x == null) {
            fail("X value", "-x");
        }
        if (y == null) {
            fail("Y value", "-y");
        }
        if (fromSosi == null) {
            fail("Source SOSI system", "-f");
        }
        if (toSosi == null) {
            fail("Destination SOSI system", "-t");
        }
    }

    int run() {
        verifyAllParameters();
        System.load(library);
        initialize(initPath);
        double[] result = new double[3];
        int returnValue = xSosiTrans(fromSosi, x, y, 0d, toSosi, result);
        if (returnValue > 0) {
            System.err.println("Error " + returnValue + ": " + getLastError());
            return returnValue;
        }
        System.out.println("(" + result[0] + ", " + result[1] + ") [" + returnValue + "]");
        return 0;
    }

    int runInSeparataProcess(String executable) {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(executable, library, initPath, Integer.toString(fromSosi), Double.toString(x), Double.toString(y), Integer.toString(toSosi));
        int ret = 42;
        try {
            Process proc = builder.start();
            ret = proc.waitFor();
            System.out.printf("[%d]\n", ret);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return ret > 0 ? ret : 0;
    }
}

public class Test {
    public static void main(String [] args) {
        Tester tester = new Tester();
        int delaySeconds = 0;
        String executable = null;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.charAt(0) == '-') {
                switch (arg) {
                    case "-l":
                        tester.setLibrary(args[++i]);
                        continue;
                    case "-i":
                        tester.setInitPath(args[++i]);
                        continue;
                    case "-x":
                        tester.setX(Double.parseDouble(args[++i]));
                        continue;
                    case "-y":
                        tester.setY(Double.parseDouble(args[++i]));
                        continue;
                    case "-f":
                        tester.setFromSosi(Integer.parseInt(args[++i]));
                        continue;
                    case "-t":
                        tester.setToSosi(Integer.parseInt(args[++i]));
                        continue;
                    case "-d":
                        delaySeconds = Integer.parseInt(args[++i]);
                        continue;
                    case "-p":
                        executable = args[++i];
                        continue;
                }
            } else {
                System.err.println("Invalid argument " + arg);
                System.exit(1);
            }
        }
        if (delaySeconds > 0) {
            try {
                Thread.sleep(delaySeconds * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (executable != null) {
            System.exit(tester.runInSeparataProcess(executable));
        }
        System.exit(tester.run());
    }
}