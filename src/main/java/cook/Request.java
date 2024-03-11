package cook;

import java.util.Set;

public class Request {
    
    private final Methods method;
    private final String uri;
    private final String host;
    private final Set<Header> headers;
    private final String body;

    public Request(Methods method, String uri, String host, Set<Header> headers, String body) {
        this.method = method;
        this.uri = uri;
        this.host = host;
        this.headers = headers;
        this.body = body;
    }
    
    public String uri() {
        return uri;
    }

}