package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {

    public static void main(String[] args) {

        String serverAddress = "localhost";
        int port = 5050;

        try (
            Socket socket = new Socket(serverAddress, port);

            BufferedReader input = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );

            PrintWriter output = new PrintWriter(
                socket.getOutputStream(), true
            );

            BufferedReader keyboard = new BufferedReader(
                new InputStreamReader(System.in)
            )
        ) {

            System.out.println("Connected to MultiChat!");

            // Read messages from the server in a separate thread.
            Thread receiver = new Thread(() -> {
                try {
                    String message;

                    while ((message = input.readLine()) != null) {
                        System.out.println("\n" + message);
                        System.out.print("You: ");
                    }

                } catch (IOException e) {
                    System.out.println("\nDisconnected from server.");
                }
            });

            receiver.setDaemon(true);
            receiver.start();

            String message;

            while ((message = keyboard.readLine()) != null) {

                if (message.equalsIgnoreCase("/exit")) {
                    output.println("LOGOUT");
                    break;
                }

                output.println(message);
            }

        } catch (IOException e) {
            System.out.println(
                "Connection error: " + e.getMessage()
            );
        }
    }
}