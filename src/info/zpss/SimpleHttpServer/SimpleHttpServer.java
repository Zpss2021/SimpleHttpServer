package info.zpss.SimpleHttpServer;

import info.zpss.SimpleHttpServer.HttpObj.*;

import java.io.*;
import java.net.ServerSocket;

public class SimpleHttpServer implements Arguable {
    private String host;
    private int port;
    private String rootDir;
    private RouteMap routeMap;
    private static final SimpleHttpServer INSTANCE;

    static {
        INSTANCE = new SimpleHttpServer();
        INSTANCE.host = null;
        INSTANCE.port = 80;
        INSTANCE.rootDir = System.getProperty("user.dir");
        INSTANCE.routeMap = new RouteMap();
        INSTANCE.initRouteMap();
    }

    private SimpleHttpServer() {
    }

    public static SimpleHttpServer getInstance() {
        return INSTANCE;
    }

    public String getHost() {
        return host;
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
        // TODO
        Response response = handler.handle(request);
        try (OutputStream out = request.getSocket().getOutputStream()) {
            out.write(response.toBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();    // TODO
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
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("HTTP Server is listening on port " + port);
            while (Thread.currentThread().isAlive())
                new ServerThread(serverSocket.accept()).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init(String[] args) {
        String hostStr = Arguable.stringInArgs(args, "-H", "--host");
        String portStr = Arguable.stringInArgs(args, "-P", "--port");
        if (hostStr != null) {
            host = hostStr;
            System.out.println("HTTP Host: " + host);
        }
        if (portStr != null) {
            port = Integer.parseInt(portStr);
            System.out.println("HTTP Port: " + port);
        }
    }
}