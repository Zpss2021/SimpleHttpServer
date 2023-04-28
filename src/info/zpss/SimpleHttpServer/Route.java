package info.zpss.SimpleHttpServer;

import info.zpss.SimpleHttpServer.HttpObj.HttpMethod;

public class Route {
    private final HttpMethod method;
    private final String path;

    public Route(HttpMethod method, String path) {
        this.method = method;
        this.path = path;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }
}