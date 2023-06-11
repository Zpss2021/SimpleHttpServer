package info.zpss.SimpleHttpServer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ServerThread extends Thread {
    private final Socket socket;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            InputStreamReader in = new InputStreamReader(socket.getInputStream(),
                    StandardCharsets.UTF_8);
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[1024];
            int len;
            while (true) {
                len = in.read(buffer);
                if (len == -1)
                    break;
                sb.append(buffer, 0, len);
                if (len < 1024)
                    break;
            }
            Request request = new Request.Builder()
                    .socket(socket)
                    .requestStr(sb.toString())
                    .build();
            if (request == null)
                System.out.printf("[Received] Invalid request: \n%s\n%n", sb);
            else {
                System.out.println("[Received] " + request.getMethod() + " " + request.getPath());
                SimpleHttpServer.getInstance().handleRequest(request);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
