package cook;

import static cook.Responses.NOT_FOUND;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server implements AutoCloseable {

    private final int port;
    private final List<Rule> rules;
    private final AtomicBoolean running;

    public Server(int port, List<Rule> rules) {
        this.port = port;
        this.rules = rules;
        this.running = new AtomicBoolean(false);
    }

    public void run() throws IOException {
        running.set(true);
        try (var serverSocket = new ServerSocket(port)) {
            while (running.get()) {
                waitForClientAndServe(serverSocket);
            }
        }
    }

    private void waitForClientAndServe(ServerSocket serverSocket) throws IOException {
        var clientSocket = serverSocket.accept();
        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            Request request = readRequest(reader);
            Optional<Rule> rule = handlerFor(request);
            RequestHandler handler = rule.isPresent() ? (RequestHandler) rule.get() : NOT_FOUND;
            handle(clientSocket, handler, request);
        } catch (IOException e) {
            System.err.println("Error reading from or writing to client: " + e.getMessage());
        } finally {
            // Close the client socket
            clientSocket.close();
            System.out.println("Client disconnected");
        }
    }

    /* This will look really nasty for a while... */
    private Request readRequest(BufferedReader reader) throws IOException {
        StringBuilder requestBuilder = new StringBuilder();

        String line = reader.readLine();
        requestBuilder.append(line).append("\r\n");

        String[] parts = line.split(" ");
        var method = Methods.valueOf(parts[0]);
        var uri = parts[1];

        while (line != null && !line.isEmpty()) {
            requestBuilder.append(line).append("\r\n");
            line = reader.readLine();
        }
        return new Request(method, uri, null, null, null);
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
