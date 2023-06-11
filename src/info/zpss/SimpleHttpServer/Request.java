package info.zpss.SimpleHttpServer;

import info.zpss.SimpleHttpServer.HttpObj.HttpMethod;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Request {
    private final HttpMethod method;
    private final String path;
    private final Map<String, String> headers;
    private final Map<String, String> params;
    private final String body;
    private final Socket socket;

    private Request(Builder builder) {
        this.method = builder.method;
        this.path = builder.path;
        this.headers = builder.headers;
        this.params = builder.params;
        this.body = builder.body;
        this.socket = builder.socket;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public String getBody() {
        return body;
    }

    public Socket getSocket() {
        return socket;
    }

    public static class Builder {
        private Socket socket;
        private String requestStr;
        private HttpMethod method;
        private String path;
        private final Map<String, String> headers;
        private final Map<String, String> params;
        private String body;

        public Builder() {
            headers = new HashMap<>();
            params = new HashMap<>();
        }

        public Builder socket(Socket socket) {
            this.socket = socket;
            return this;
        }

        public Builder requestStr(String requestStr) {
            this.requestStr = requestStr;
            return this;
        }

        public Request build() {
            if (requestStr == null)
                return null;
            String[] content = requestStr.split("\r\n");
            String[] firstLine = content[0].split(" ");
            this.method = HttpMethod.fromString(firstLine[0]);
            this.path = firstLine[1];
            for (int i = 1; i < content.length; i++) {
                if (content[i].isEmpty())
                    break;
                String[] header = content[i].split(": ");
                this.headers.put(header[0], header[1]);
            }

            if (this.method == null)
                return null;
            if (this.method == HttpMethod.GET || this.method == HttpMethod.POST) {
                int index = this.path.indexOf("?"); // 问号是为了分隔路径和参数，例如：/index.html?name=Alice&age=20
                if (index != -1) {
                    String[] params = this.path.substring(index + 1).split("&");
                    for (String param : params) {
                        String[] kv = param.split("=");
                        this.params.put(kv[0], kv[1]);
                    }
                    this.path = this.path.substring(0, index);
                }
            }
            if (this.method == HttpMethod.POST)
                this.body = content[content.length - 1];
            else
                this.body = null;

            if (this.path.equals("/"))      // 默认路径为 /index.html
                this.path = "/index.html";

            if (this.path.endsWith("/"))    // 为了方便处理，去掉路径最后的斜杠
                this.path = this.path.substring(0, this.path.length() - 1);

            if (this.path.contains(".."))   // 防止路径穿越，例如：/index.html/../../etc/passwd
                return null;

            if (SimpleHttpServer.getInstance().getHost() != null)
                if (this.headers.containsKey("Referer"))    // 防止盗链，例如：Referer: http://evil.com
                    if (!this.headers.get("Referer").contains(SimpleHttpServer.getInstance().getHost()))
                        return null;

            if (this.headers.containsKey("Host"))
                if (this.headers.get("Host").startsWith("uniwood.zpss.info") && this.path.equals("/index.html"))
                    this.path = "/uniwood.html";
            return new Request(this);
        }
    }
}