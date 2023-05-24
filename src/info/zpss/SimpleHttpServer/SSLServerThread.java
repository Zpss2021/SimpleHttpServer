package info.zpss.SimpleHttpServer;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class SSLServerThread extends Thread {
    private final SSLSocket sslSocket;

    public SSLServerThread(SSLSocket sslSocket) {
        this.sslSocket = sslSocket;
    }

    @Override
    public void run() {
        try {
            InputStreamReader in = new InputStreamReader(sslSocket.getInputStream(),
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
                    .socket(sslSocket)
                    .requestStr(sb.toString())
                    .build();
            System.out.println("[Received] " + request.getMethod() + " " + request.getPath());
            SimpleHttpServer.getInstance().handleRequest(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
