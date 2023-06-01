package info.zpss.SimpleHttpServer;

import info.zpss.SimpleHttpServer.HttpObj.HttpContentType;
import info.zpss.SimpleHttpServer.HttpObj.HttpStatus;

import java.text.SimpleDateFormat;
import java.util.*;

class Response {
    private final HttpStatus status;
    private final Map<String, String> headers;
    private final byte[] body;

    private Response(Builder builder) {
        this.status = builder.status;
        this.headers = builder.headers;
        this.body = builder.body;
    }

    public byte[] toBytes() {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(status.getCode()).append(" ").append(status.getReason()).append("\r\n");
        for (Map.Entry<String, String> entry : headers.entrySet())
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        sb.append("\r\n");
        byte[] head = sb.toString().getBytes();
        byte[] result = new byte[head.length + body.length];
        System.arraycopy(head, 0, result, 0, head.length);
        System.arraycopy(body, 0, result, head.length, body.length);
        return result;
    }

    public static class Builder {
        private HttpStatus status;
        private final Map<String, String> headers;
        private byte[] body;

        public Builder() {
            this.status = HttpStatus.OK;
            this.headers = new HashMap<>();
            this.body = null;    // 默认为空
            // 设置默认的响应头值
            SimpleDateFormat gmt_fmt = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
            gmt_fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
            this.headers.put("Cache-Control", "private, max-age=0, no-cache");   // 默认使浏览器不缓存
            this.headers.put("Date", gmt_fmt.format(System.currentTimeMillis()));
            this.headers.put("Server", "SimpleHttpServer/1.0");
            this.headers.put("Connection", "close");
            this.headers.put("Content-Type", "text/html; charset=utf-8");
            this.headers.put("Content-Length", "0");
            this.headers.put("Content-Security-Policy", "default-src 'self' 'unsafe-inline'");
            this.headers.put("X-XSS-Protection", "1; mode=block");
            this.headers.put("X-Frame-Options", "DENY");
            this.headers.put("X-Content-Type-Options", "nosniff");
        }

        public Builder status(HttpStatus status) {
            this.status = status;
            return this;
        }

        public Builder body(byte[] body) {
            this.body = new byte[body.length];
            System.arraycopy(body, 0, this.body, 0, body.length);
            this.headers.put("Content-Length", String.valueOf(body.length));
            return this;
        }

        public Builder header(HttpContentType contentType) {
            this.headers.put("Content-Type", contentType.getName());
            return this;
        }

        public Builder header(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        public Response build() {
            return new Response(this);
        }
    }
}