package cook;

public class TextHandler implements RequestHandler {

    private final int status;
    private final String reason;
    private final String body;

    public TextHandler(int status, String reason, String body) {
        this.status = status;
        this.reason = reason;
        this.body = body;
    }

    @Override
    public String handle(Request request) {
        return String.format("HTTP/1.1 %1$d %2$s\r\nContent-Type: text/plain\r\n\r\n%3$s", status, reason, body);
    }

}
