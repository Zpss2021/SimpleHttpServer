import info.zpss.SimpleHttpServer.SimpleHttpServer;

public class Main {
    public static void main(String[] args) {
        SimpleHttpServer server = SimpleHttpServer.getInstance();
        server.setPort(60196);
        server.setRootDir("HTMLTest");
        server.start();
    }
}