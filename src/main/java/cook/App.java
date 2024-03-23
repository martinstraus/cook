package cook;

import static cook.Methods.POST;
import static cook.RequestMatchers.uriEqualsCaseSensitive;
import static cook.RequestMatchers.withMethod;
import static cook.Responses.fromRequest;
import java.io.IOException;
import static java.util.Arrays.asList;

public class App {

    public static void main(String[] args) throws IOException {
        new Server(
            8080,
            10,
            new NoOpCallback(),
            asList(
                new SimpleRule(
                    uriEqualsCaseSensitive("/ping"),
                    new TextHandler(200, "OK", "pong")
                ),
                new SimpleRule(
                    withMethod(POST).and(uriEqualsCaseSensitive("/echo")),
                    fromRequest(200, "OK", Request::body)
                )
            )
        ).run();
    }
}
