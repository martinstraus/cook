package cook;

import java.io.IOException;
import static java.util.Collections.emptyList;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

public class CallbacksTest {
        
    @Test
    public void callbacksAreCalled() throws Exception {
        var callback = mock(Callback.class);
        try (var server = new Server(8080, 1, callback, emptyList())) {
            runServerInAnotherThread(server);
            Thread.sleep(100);
            server.close();
        } finally {
            verify(callback, times(1)).started();
            verify(callback, times(1)).finished();
        }
    }
    
    private void runServerInAnotherThread(Server server) {
        Executors.newFixedThreadPool(1).submit(() -> {
            try {
                server.run();
            } catch (IOException ex) {
                
            }
        });
    }
    
}
