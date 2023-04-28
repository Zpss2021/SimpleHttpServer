package info.zpss.SimpleHttpServer;

import java.io.IOException;
import java.net.ServerSocket;

public class SimpleHttpServer {
    public static void main(String[] args) {
        final int port = 60196;
        try (ServerSocket serverSocket = new ServerSocket(port)){
            System.out.println("Server is listening on port " + port);
            while (true)
                new Thread(new ServerThread(serverSocket.accept())).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
