package cook;

import static cook.Responses.NOT_FOUND;
import static cook.Responses.badRequest;
import static cook.Responses.internalServerError;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import static java.util.Collections.emptyList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server implements AutoCloseable {

    private final int port;
    private final int threads;
    private final List<Rule> rules;
    private final Callback callback;
    private final AtomicBoolean running;

    public Server(int port) {
        this(port, 1, new NoOpCallback(), emptyList());
    }

    public Server(int port, int threads, Callback callback, List<Rule> rules) {
        this.port = port;
        this.threads = threads;
        this.rules = rules;
        this.callback = callback;
        this.running = new AtomicBoolean(false);
    }

    public void run() throws IOException {
        running.set(true);
        var threadPool = Executors.newFixedThreadPool(threads);
        try (var serverSocket = new ServerSocket(port)) {
            callback.started();
            while (running.get()) {
                var clientSocket = serverSocket.accept();
                clientSocket.setSoTimeout(100);
                threadPool.submit(() -> serve(clientSocket));
            }
            try {
                threadPool.awaitTermination(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {

            }
        } finally {
            callback.finished();
        }
    }

    private void serve(Socket clientSocket) {
        try {
            try {
                Request request = readRequest(clientSocket.getInputStream());
                Optional<Rule> rule = handlerFor(request);
                RequestHandler handler = rule.isPresent() ? rule.get() : NOT_FOUND;
                handle(clientSocket, handler, request);
            } catch (BadRequest e) {
                handle(clientSocket, badRequest(e), null);
            }
        } catch (Throwable t) {
            try {
                handle(clientSocket, internalServerError(t), null);
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }

    /* This will look really nasty for a while... */
    private Request readRequest(InputStream stream) throws IOException, BadRequest {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        Request.RequestLine requestLine = requestLine(reader);
        Set<Header> headers = headers(reader);
        String body = null;
        if ((requestLine.method() == Methods.POST || requestLine.method() == Methods.PUT)) {
            Header contentLength = headers
                .stream()
                .filter((h) -> h.name().equalsIgnoreCase("Content-length"))
                .findFirst()
                .orElseThrow(() -> new BadRequest("Request does not specify content length."));
            byte[] buffer = new byte[contentLength.intValue()];
            int read = stream.read(buffer);
            if (read != contentLength.intValue()) {
                throw new BadRequest("Body length does not match value of Content-length header.");
            }
            body = new String(buffer);
        }
        return new Request(requestLine, headers, body);

    }

    private Request.RequestLine requestLine(BufferedReader reader) throws IOException {
        String[] parts = reader.readLine().split(" ");
        return new Request.RequestLine(
            Methods.valueOf(parts[0]),
            parts[1],
            parts[2]
        );
    }

    private Set<Header> headers(BufferedReader reader) throws IOException {
        Set<Header> headers = new HashSet<>();
        String line = reader.readLine();
        while (line != null && !line.trim().equals("")) {
            var index = line.indexOf(":");
            var key = line.substring(0, index).trim();
            var value = line.substring(index + 1).trim();
            headers.add(new Header(key, value));
            line = reader.readLine();
        }
        return headers;
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
