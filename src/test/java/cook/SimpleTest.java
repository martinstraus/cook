package cook;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.*;

public class SimpleTest {

    private static final Random RANDOM = new Random();

    @Test
    @Timeout(5000)
    public void testRunsAndReceivesPing() throws IOException, URISyntaxException, InterruptedException {
        var port = randomPort();
        var server = runServer(
            port,
            new SimpleRule(
                RequestMatchers.uriEqualsCaseSensitive("/ping"),
                new TextHandler(200, "OK", "pong")
            )
        );
        try {
            try (var client = HttpClient.newHttpClient()) {
                var request = HttpRequest.newBuilder(
                    new URI(
                        String.format("http://localhost:%d/ping", port)
                    )
                ).build();
                var response = client.send(request, BodyHandlers.ofString());
                assertEquals(200, response.statusCode());
                assertEquals("pong", response.body());
            } catch (IOException ex) {
                fail("Error sending request.", ex);
            }
        } finally {
            if (server != null) {
                server.stop();
            }
        }
    }

    @Test
    @Timeout(5000)
    public void notFoundForRandomResource() throws IOException, URISyntaxException, InterruptedException {
        var port = randomPort();
        var server = runServer(port);
        try {
            try (var client = HttpClient.newHttpClient()) {
                var request = HttpRequest.newBuilder(
                    new URI(
                        String.format("http://localhost:%d/%s", port, UUID.randomUUID().toString())
                    )
                ).build();
                var response = client.send(request, BodyHandlers.ofString());
                assertEquals(404, response.statusCode());
            } catch (IOException ex) {
                fail("Error sending request.", ex);
            }
        } finally {
            if (server != null) {
                server.stop();
            }
        }
    }

    private int randomPort() {
        return RANDOM.nextInt(8080, 8090);
    }

    private Server runServer(int port, Rule... rules) {
        var server = new Server(port, rules != null ? asList(rules) : emptyList());
        var runServer = (Callable) () -> {
            try {
                server.run();
            } catch (IOException ex) {
                Logger.getLogger(SimpleTest.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                return null;
            }
        };
        Executors.newFixedThreadPool(1).submit(runServer);
        return server;
    }
}
