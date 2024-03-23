package cook;

import cook.Header;
import cook.Methods;
import cook.Request;
import cook.RequestHandler;
import static cook.Responses.NOT_FOUND;
import cook.Rule;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server implements AutoCloseable {

    private final int port;
    private final int threads;
    private final List<Rule> rules;
    private final Callback callback;
    private final AtomicBoolean running;

    public Server(int port, int threads, Callback callback, List<Rule> rules) {
        this.port = port;
        this.threads = threads;
        this.rules = rules;
        this.callback = callback;
        this.running = new AtomicBoolean(false);
    }

    public void run() throws IOException {
        running.set(true);
        try (var serverSocket = new ServerSocket(port)) {
            var pool = Executors.newFixedThreadPool(threads);
            for (int i = 0; i < threads; i++) {
                pool.submit(() -> waitForClientAndServe(serverSocket));
            }
            callback.started();
            while (running.get()) {
                try {
                    pool.awaitTermination(10, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } finally {
            callback.finished();
        }
    }

    private void waitForClientAndServe(ServerSocket serverSocket) {
        while (running.get()) {
            try (
                var clientSocket = serverSocket.accept(); var inputStream = clientSocket.getInputStream(); var inputStreamReader = new InputStreamReader(inputStream);) {
                Request request = readRequest(inputStream, inputStreamReader);
                Optional<Rule> rule = handlerFor(request);
                RequestHandler handler = rule.isPresent() ? (RequestHandler) rule.get() : NOT_FOUND;
                handle(clientSocket, handler, request);
            } catch (IOException e) {
                System.err.println("Error reading from or writing to client: " + e.getMessage());
            }
        }
    }

    /* This will look really nasty for a while... */
    private Request readRequest(InputStream stream, InputStreamReader reader) throws IOException {
        int available = stream.available();
        final char[] buffer = new char[available];

        reader.read(buffer);
        var request = new String(buffer);
        try (var scanner = new Scanner(request)) {
            scanner.useDelimiter("\r\n");
            String line = scanner.next();

            String[] parts = line.split(" ");
            var method = Methods.valueOf(parts[0]);
            var uri = parts[1];

            Set<Header> headers = new HashSet<>();
            while (scanner.hasNext()) {
                line = scanner.next();
                if (line.isBlank()) {
                    break;
                }
                var index = line.indexOf(":");
                var key = line.substring(1, index).trim();
                var value = line.substring(index + 1).trim();
                headers.add(new Header(key, value));
            }

            String body = null;
            if (scanner.hasNext()) {
                StringBuilder b = new StringBuilder();
                while (scanner.hasNext()) {
                    b.append(scanner.next());
                }
                body = b.toString();
            }
            return new Request(method, uri, null, headers, body);
        }

    }

    private Optional<Rule> handlerFor(Request request) {
        return rules.stream().filter((r) -> r.test(request)).findFirst();
    }

    private void handle(Socket clientSocket, RequestHandler handler, Request request) throws IOException {
        try (OutputStream outputStream = clientSocket.getOutputStream()) {
            outputStream.write(handler.handle(request).getBytes());
            outputStream.flush();
        }
    }

    @Override
    public void close() throws Exception {
        running.set(false);

    }
}
