package cook;

public class Ping implements Rule {

    @Override
    public String handle(Request request) {
        final String body = "pong";
        return "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-length: %1$d\r\n\r\n%2$s"
            .formatted(body.length(), body);
    }

    @Override
    public boolean test(Request request) {
        return request.hasMethod(Methods.GET) && request.uriEqualsMatchCase("/ping");
    }

}
