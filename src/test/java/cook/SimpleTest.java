package cook;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Random;
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
    public void testRunsAndReceivesPing() throws IOException {
        var port = RANDOM.nextInt(8080, 8090);
        var server = new Server(port);
        try {
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
            try (var client = HttpClient.newHttpClient()) {
                var request = HttpRequest.newBuilder(
                    new URI(
                        String.format("http://localhost:%d/ping", port)
                    )
                ).build();
                var response = client.send(request, BodyHandlers.ofString());
                assertEquals("pong", response.body());
            } catch (IOException ex) {
                fail("Error sending request.", ex);
            }
        } catch (Throwable t) {
            server.stop();
            fail("Error with server.", t);
        }
    }
}
