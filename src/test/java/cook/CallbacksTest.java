package cook;

import java.io.IOException;
import static java.util.Collections.emptyList;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

public class CallbacksTest {

    @Test
    public void callbacksAreCalled() throws Exception {
        var callback = mock(Callback.class);
        try (var server = new Server(8080, 1, callback, emptyList())) {
            runServerInAnotherThread(server);
            Thread.sleep(100);
        }
        verify(callback, times(1)).started();
    }

    private void runServerInAnotherThread(Server server) {
        Runnable task = () -> {
            try {
                server.run();
            } catch (IOException ex) {

            }
        };
        new Thread(task, "CallbacksTest server").start();
    }

}
