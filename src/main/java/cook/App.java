package cook;

import java.io.IOException;
import static java.util.Arrays.asList;

public class App {

    public static void main(String[] args) throws IOException {
        new Server(
            8080,
            asList(
                new SimpleRule(
                    RequestMatchers.uriEqualsCaseSensitive("/ping"),
                    new TextHandler(200, "OK", "pong")
                )
            )
        ).run();
    }
}
