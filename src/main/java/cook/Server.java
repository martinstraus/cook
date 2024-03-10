package cook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private final int port;
    private ServerSocket serverSocket;

    public Server(int port) {
        this.port = port;
    }

    public void run() throws IOException {
        serverSocket = new ServerSocket(port);
        var clientSocket = serverSocket.accept();
        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String request = readRequest(reader);
            System.out.println("Received request:\n" + request);
            handle(clientSocket, request);
        } catch (IOException e) {
            System.err.println("Error reading from or writing to client: " + e.getMessage());
        } finally {
            // Close the client socket
            clientSocket.close();
            System.out.println("Client disconnected");
        }
    }

    private String readRequest(BufferedReader reader) throws IOException {
        StringBuilder requestBuilder = new StringBuilder();
        String line = reader.readLine();
        while (line != null && !line.isEmpty()) {
            requestBuilder.append(line).append("\r\n");
            line = reader.readLine();
        }
        return requestBuilder.toString();
    }

    private void handle(Socket clientSocket, String request) throws IOException {
        try (OutputStream outputStream = clientSocket.getOutputStream()) {
            String response = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\npong";
            outputStream.write(response.getBytes());
            outputStream.flush();
        }
    }

    public void stop() throws IOException {
        serverSocket.close();
    }
}
