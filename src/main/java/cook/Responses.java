package cook;

import java.util.function.Function;

public class Responses {

    public static final String MESSAGE_WITH_BODY = """
                                                   HTTP/1.1 %1$d %2$s
                                                   Content-Type: text/plain
                                                   Content-length: %3$d
                                           
                                                   %4$s
                                                   """;

    public static RequestHandler NOT_FOUND = new TextHandler(404, "Not found", "");

    public static RequestHandler fromRequest(int statusCode, String reason, Function<Request, String> bodyFunction) {
        return (Request r) -> {
            String body = bodyFunction.apply(r);
            return MESSAGE_WITH_BODY.formatted(statusCode, reason, body.length(), body);
        };
    }
    
    public static RequestHandler badRequest(BadRequest exception) {
        return (Request r) -> {
            String body = exception.getMessage();
            return MESSAGE_WITH_BODY.formatted(400, "Bad-request", body.length(), body);
        };
    }
    
    public static RequestHandler internalServerError(Throwable throwable) {
        return (Request r) -> {
            String body = throwable.getMessage();
            return MESSAGE_WITH_BODY.formatted(503, "Internal-server-error", body.length(), body);
        };
    }
}
