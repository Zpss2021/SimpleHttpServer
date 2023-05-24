package info.zpss.SimpleHttpServer;

import info.zpss.SimpleHttpServer.HttpObj.HttpContentType;
import info.zpss.SimpleHttpServer.HttpObj.HttpMethod;
import info.zpss.SimpleHttpServer.HttpObj.HttpStatus;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import java.io.*;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class SimpleHttpsServer implements Arguable {
    private int port;
    private String rootDir;
    private RouteMap routeMap;
    private SSLContext sslContext;
    private static final SimpleHttpsServer INSTANCE;

    static {
        INSTANCE = new SimpleHttpsServer();
        INSTANCE.port = 443;
        INSTANCE.rootDir = System.getProperty("user.dir");
        INSTANCE.routeMap = new RouteMap();
        INSTANCE.initRouteMap();
    }

    private SimpleHttpsServer() {
    }

    public static SimpleHttpsServer getInstance() {
        return INSTANCE;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setRootDir(String rootDir) {
        this.rootDir = System.getProperty("user.dir") + File.separatorChar + rootDir;
    }

    public void setAbsoluteRootDir(String absoluteRootDir) {
        this.rootDir = absoluteRootDir;
    }

    public void handleRequest(Request request) {
        RequestHandler handler = routeMap.get(request.getMethod(), request.getPath());
        if (handler == null)
            handler = routeMap.get(HttpMethod.GET, "/*");
        Response response = handler.handle(request);
        try (OutputStream out = request.getSocket().getOutputStream()) {
            out.write(response.toBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();    // TODO
        }
    }

    private void initSSL(String crtFilePath, String keyFilePath) {
        try {
            // 读取证书文件
            FileInputStream certificateInputStream = new FileInputStream(crtFilePath);
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate
                    = (X509Certificate) certificateFactory.generateCertificate(certificateInputStream);

            // 读取私钥文件
            FileInputStream privateKeyInputStream = new FileInputStream(keyFilePath);
            byte[] privateKeyBytes = new byte[privateKeyInputStream.available()];
            int n = privateKeyInputStream.read(privateKeyBytes);
            if (n != privateKeyBytes.length)
                throw new IOException("Failed to read private key file");
            privateKeyInputStream.close();
            char[] privateKey = new String(privateKeyBytes).toCharArray();

            // 创建KeyStore并导入证书
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            keyStore.setCertificateEntry("alias", certificate);

            // 创建KeyManagerFactory并初始化
            KeyManagerFactory keyManagerFactory
                    = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, privateKey);

            // 创建SSLContext
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initRouteMap() {
        RequestHandler handleIndex = request -> {
            File file = new File(rootDir + File.separatorChar + "index.html");
            return getResponse(file);
        };
        RequestHandler handleDefault = request -> {
            File file = new File(rootDir + File.separatorChar + request.getPath());
            if (!file.exists())
                return new Response.Builder()
                        .status(HttpStatus.NOT_FOUND)
                        .header(HttpContentType.TEXT_HTML)
                        .body("<h1>404 Not Found</h1>".getBytes())
                        .build();
            return getResponse(file);
        };
        addRoute(new Route(HttpMethod.GET, "/"), handleIndex);
        addRoute(new Route(HttpMethod.GET, "/index.html"), handleIndex);
        addRoute(new Route(HttpMethod.GET, "/*"), handleDefault);
        addRoute(new Route(HttpMethod.GET, "/path"), request -> {
            String path = System.getProperty("user.dir");
            return new Response.Builder()
                    .status(HttpStatus.OK)
                    .header(HttpContentType.TEXT_HTML)
                    .body(("<h1>Path: " + path + "</h1>").getBytes())
                    .build();
        });
    }

    private Response getResponse(File file) {
        byte[] content = new byte[(int) file.length()];
        try (FileInputStream fin = new FileInputStream(file)) {
            int len = fin.read(content);
            if (len != content.length)
                throw new IOException("Read file \"" + file.getAbsolutePath() + "\" error");
        } catch (IOException e) {
            e.printStackTrace();    // TODO
        }
        String fileName = file.getName();
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
        return new Response.Builder()
                .status(HttpStatus.OK)
                .header(HttpContentType.fromString(extension))
                .body(content)
                .build();
    }

    private void addRoute(Route route, RequestHandler handler) {
        routeMap.put(route, handler);
    }

    public void start() {
        // 创建SSLServerSocket
        int port = 443; // HTTPS默认端口为443
        try (SSLServerSocket sslServerSocket
                     = (SSLServerSocket) sslContext.getServerSocketFactory().createServerSocket(port)) {
            // 配置SSLServerSocket
            String[] enabledCipherSuites = {
                    "TLS_RSA_WITH_AES_256_CBC_SHA256",
                    "TLS_RSA_WITH_AES_128_CBC_SHA256",
                    "TLS_RSA_WITH_AES_256_CBC_SHA",
                    "TLS_RSA_WITH_AES_128_CBC_SHA"
            };  // 使用指定的加密套件
            sslServerSocket.setEnabledCipherSuites(enabledCipherSuites);
            String[] enabledProtocols = {
                    "TLSv1.2",
                    "TLSv1.1",
                    "TLSv1"
            };  // 使用指定的协议
            sslServerSocket.setEnabledProtocols(enabledProtocols);
            sslServerSocket.setNeedClientAuth(false); // 可选，根据需要配置是否需要客户端身份验证
            System.out.println("HTTPS Server is listening on port " + port);
            while (Thread.currentThread().isAlive())
                new SSLServerThread((SSLSocket) sslServerSocket.accept()).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init(String[] args) {
        System.out.println("HTTPS port: " + port);
        String crtFilePath = Arguable.stringInArgs(args, "-c", "--cert");
        if (crtFilePath == null)
            throw new IllegalArgumentException("Certificate file path is required");
        String keyFilePath = Arguable.stringInArgs(args, "-k", "--key");
        if (keyFilePath == null)
            throw new IllegalArgumentException("Private key file path is required");
        initSSL(crtFilePath, keyFilePath);
    }
}