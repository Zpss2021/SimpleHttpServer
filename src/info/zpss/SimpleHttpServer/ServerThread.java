package info.zpss.SimpleHttpServer;

import java.io.*;
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
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(),
                    StandardCharsets.UTF_8));
            OutputStream out = socket.getOutputStream();
            String line = in.readLine();
            if (line == null) {
                socket.close();
                return;
            }
            System.out.println("[Received] " + line);
            String filePath = line.split(" ")[1].substring(1);
            if (filePath.equals(""))
                filePath = "index.html";
            try (FileInputStream fin = new FileInputStream(filePath)) {
                out.write("HTTP/1.1 200 OK\r\n".getBytes(StandardCharsets.UTF_8));
                String sType = filePath.substring(filePath.lastIndexOf(".") + 1);
                switch (sType) {
                    case "css":
                        out.write("Content-Type: text/css; charset=utf-8\r\n".getBytes(StandardCharsets.UTF_8));
                        break;
                    case "js":
                        out.write("Content-Type: text/javascript; charset=utf-8\r\n".getBytes(StandardCharsets.UTF_8));
                        break;
                    case "png":
                        out.write("Content-Type: image/png\r\n".getBytes(StandardCharsets.UTF_8));
                        break;
                    case "jpg":
                        out.write("Content-Type: image/jpeg\r\n".getBytes(StandardCharsets.UTF_8));
                        break;
                    case "gif":
                        out.write("Content-Type: image/gif\r\n".getBytes(StandardCharsets.UTF_8));
                        break;
                    case "htm":
                    case "html":
                    default:
                        out.write("Content-Type: text/html\r\n".getBytes(StandardCharsets.UTF_8));
                        break;
                }
                out.write("\r\n".getBytes(StandardCharsets.UTF_8));
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fin.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                    out.flush();
                }
            } catch (FileNotFoundException e) {
                out.write("HTTP/1.1 404 Not Found\r\n".getBytes(StandardCharsets.UTF_8));
                out.write("Content-Type: text/html\r\n".getBytes(StandardCharsets.UTF_8));
                out.write("\r\n".getBytes(StandardCharsets.UTF_8));
                out.write("<h1>404 Not Found</h1>".getBytes(StandardCharsets.UTF_8));
            } finally {
                out.close();
                in.close();
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
