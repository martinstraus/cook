package cook;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author martinstraus
 */
public class TestClient {

    public static void main(String[] args) throws URISyntaxException {
        AtomicInteger count = new AtomicInteger(0);
        var uri = new URI("http://localhost:8080/ping");
        for (int i = 0; i < 5; i++) {
            new Thread(
                () -> {
                    var thread = Thread.currentThread().getName();
                    count.incrementAndGet();
                    try (var client = HttpClient.newHttpClient()) {
                        for (int j = 0; j < 100; j++) {
                            var request = HttpRequest.newBuilder(uri).build();
                            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
                            System.out.printf("[%s] Response: %d - %s\n", thread, response.statusCode(), response.body());
                            Thread.sleep(100);
                        }
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                    } finally {
                        count.decrementAndGet();
                    }
                },
                String.format("client %d", i)
            ).start();
        }
        while (count.get() > 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(TestClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
