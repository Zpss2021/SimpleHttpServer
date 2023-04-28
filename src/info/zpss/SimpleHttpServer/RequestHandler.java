package info.zpss.SimpleHttpServer;

public interface RequestHandler {
    Response handle(Request request);
}
