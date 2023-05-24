import info.zpss.SimpleHttpServer.Arguable;
import info.zpss.SimpleHttpServer.SimpleHttpServer;
import info.zpss.SimpleHttpServer.SimpleHttpsServer;

public class Main implements Arguable {
    public static void main(String[] args) {
        new Main().init(args);
        new Thread(() -> {
            SimpleHttpServer httpServer = SimpleHttpServer.getInstance();
            httpServer.init(args);
            httpServer.start();
        }).start();
        if (Arguable.paramInArgs(args, "-s", "--ssl")) {
            new Thread(() -> {
                SimpleHttpsServer httpsServer = SimpleHttpsServer.getInstance();
                httpsServer.init(args);
                httpsServer.start();
            }).start();
        }
    }

    @Override
    public void init(String[] args) {
        if (Arguable.paramInArgs(args, "-h", "--help")) {
            System.out.println("Usage: java -jar SimpleHttpServer.jar [options]");
            System.out.println("Options:");
            System.out.println("  -h, --help\t\t\tShow this help message and exit");
            System.out.println("  -s, --ssl\t\t\tEnable HTTPS server");
            System.out.println("  -p, --port <port>\t\tSpecify the HTTP port to listen on");
            System.out.println("  -d, --dir <dir>\t\tSpecify the root directory");
            System.out.println("  -a, --absolute-dir <dir>\tSpecify the absolute root directory");
            System.out.println("  -c, --cert <cert>\t\tSpecify the certificate file for HTTPS");
            System.out.println("  -k, --key <key>\t\tSpecify the private key file for HTTPS");
            System.exit(0);
        }
        String dir = Arguable.stringInArgs(args, "-d", "--dir");
        if (dir != null) {
            System.out.println("Root directory: " + dir);
            SimpleHttpServer.getInstance().setRootDir(dir);
            SimpleHttpsServer.getInstance().setRootDir(dir);
        }
        String absDir = Arguable.stringInArgs(args, "-a", "--absolute-dir");
        if (absDir != null) {
            System.out.println("Absolute root directory: " + absDir);
            SimpleHttpServer.getInstance().setAbsoluteRootDir(absDir);
            SimpleHttpsServer.getInstance().setAbsoluteRootDir(absDir);
        }
    }
}