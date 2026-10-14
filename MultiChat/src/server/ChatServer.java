package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {

    private static final int PORT = 5050;

    public static void main(String[] args) {

        System.out.println("Starting MultiChat Server...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("Server started successfully!");
            System.out.println("Waiting for clients...");

            while (true) {

                Socket socket = serverSocket.accept();

                System.out.println(
                    "New connection: " + socket.getRemoteSocketAddress()
                );

                Thread thread = new Thread(
                    new ClientHandler(socket)
                );

                thread.start();
            }

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }
}