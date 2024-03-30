package cook;

import java.util.Set;

public class Request {

    public static record RequestLine(Methods method, String uri, String version) {

    }

    private final RequestLine requestLine;
    private final Set<Header> headers;
    private final String body;

    public Request(RequestLine requestLine, Set<Header> headers, String body) {
        this.requestLine = requestLine;
        this.headers = headers;
        this.body = body;
    }

    public boolean hasMethod(Methods method) {
        return this.requestLine.method() == method;
    }

    public boolean uriEqualsMatchCase(String test) {
        return this.requestLine.uri.equals(test);
    }

    public String body() {
        return body;
    }

}
