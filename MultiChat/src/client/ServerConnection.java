package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerConnection {

    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 5050;

    public boolean connect() {
        try {
            socket = new Socket(SERVER_ADDRESS, PORT);

            input = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            output = new PrintWriter(
                    socket.getOutputStream(), true);

            // Consume the three introductory server messages.
            for (int i = 0; i < 3; i++) {
                if (input.readLine() == null) {
                    disconnect();
                    return false;
                }
            }

            return true;

        } catch (IOException e) {
            System.out.println("Connection failed: " + e.getMessage());
            disconnect();
            return false;
        }
    }

    public synchronized void sendCommand(String command) {
        if (output != null && !socket.isClosed()) {
            output.println(command);
        }
    }

    public String readResponse() {
        try {
            return input == null ? null : input.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    public void disconnect() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Error closing connection.");
        }
    }

    public boolean isConnected() {
        return socket != null
                && socket.isConnected()
                && !socket.isClosed();
    }
}