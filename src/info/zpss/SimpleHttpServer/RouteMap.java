package info.zpss.SimpleHttpServer;

import info.zpss.SimpleHttpServer.HttpObj.HttpMethod;

import java.util.HashMap;
import java.util.Map;

public class RouteMap {
    private final Map<Route, RequestHandler> map;

    public RouteMap() {
        map = new HashMap<>();
    }

    public void put(Route route, RequestHandler handler) {
        map.put(route, handler);
    }

    public RequestHandler get(Route route) {
        return map.get(route);
    }

    public RequestHandler get(HttpMethod method, String path) {
        for (Route route : map.keySet())
            if (route.getMethod() == method && route.getPath().equals(path))
                return map.get(route);
        return null;
    }
}