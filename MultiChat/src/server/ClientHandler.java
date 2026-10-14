package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import utils.MessageStore;
import utils.UserStore; 
public class ClientHandler implements Runnable {

    // Stores currently connected users and their connections.
    private static final Map<String, ClientHandler> onlineUsers =
            new ConcurrentHashMap<>();

    private final Socket socket;
    private PrintWriter output;
    private String username;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    // Used by the server to find a connected user.
    public static Map<String, ClientHandler> getOnlineUsers() {
        return onlineUsers;
    }

    @Override
    public void run() {

        try (
            BufferedReader input = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            )
        ) {

            output = new PrintWriter(socket.getOutputStream(), true);

            output.println("Welcome to MultiChat!");
            output.println("Commands: LOGIN username password | REGISTER username password");
            output.println("After login, use: MSG username message | USERS | LOGOUT");

            String request;

            while ((request = input.readLine()) != null) {

                String[] parts = request.split(" ", 3);

                switch (parts[0].toUpperCase()) {

                    case "REGISTER":
                        register(parts);
                        break;

                    case "LOGIN":
                        login(parts);
                        break;

                    case "MSG":
                        sendPrivateMessage(parts);
                        break;

                    case "USERS":
                        listUsers();
                        break;

                    case "LOGOUT":
                        output.println("LOGOUT_SUCCESS");
                        return;

                    default:
                        output.println("ERROR Unknown command.");
                }
            }

        } catch (IOException e) {

            System.out.println("Connection ended for " + username);

        } finally {

            if (username != null) {
                onlineUsers.remove(username, this);
                System.out.println(username + " disconnected.");
            }

            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error closing socket.");
            }
        }
    }

    private void register(String[] parts) {

        if (parts.length != 3) {
            output.println("ERROR Usage: REGISTER username password");
            return;
        }

        String newUsername = parts[1];
        String password = parts[2];

        if (newUsername.isBlank() || password.isBlank()) {
            output.println("ERROR Username and password are required.");
            return;
        }

        synchronized (ClientHandler.class) {

            Map<String, String> users = UserStore.loadUsers();

            if (users.containsKey(newUsername)) {
                output.println("ERROR Username already exists.");
                return;
            }

            users.put(newUsername, password);
            UserStore.saveUsers(users);
        }

        output.println("REGISTER_SUCCESS");
    }

    private void login(String[] parts) {

        if (parts.length != 3) {
            output.println("ERROR Usage: LOGIN username password");
            return;
        }

        if (username != null) {
            output.println("ERROR You are already logged in.");
            return;
        }

        String requestedUsername = parts[1];
        String password = parts[2];

        Map<String, String> users = UserStore.loadUsers();

        if (!password.equals(users.get(requestedUsername))) {
            output.println("ERROR Invalid username or password.");
            return;
        }

        ClientHandler existing =
                onlineUsers.putIfAbsent(requestedUsername, this);

        if (existing != null) {
            output.println("ERROR User is already logged in.");
            return;
        }

        username = requestedUsername;

        output.println("LOGIN_SUCCESS " + username);
        System.out.println(username + " logged in.");
    }

    private void sendPrivateMessage(String[] parts) {

    if (username == null) {
        output.println("ERROR Please log in first.");
        return;
    }

    if (parts.length != 3 || parts[2].isBlank()) {
        output.println("ERROR Usage: MSG username message");
        return;
    }

    String recipient = parts[1];
    String message = parts[2];

    if (recipient.equalsIgnoreCase(username)) {
        output.println("ERROR You cannot message yourself.");
        return;
    }

    ClientHandler target = onlineUsers.get(recipient);

    if (target == null) {
        output.println("ERROR User is not online.");
        return;
    }

    // Persist the message before delivering it.
    MessageStore.saveMessage(username, recipient, message);

    target.output.println("[Private] " + username + ": " + message);
    output.println("[Private to " + recipient + "] " + message);
}

    private void listUsers() {

        if (username == null) {
            output.println("ERROR Please log in first.");
            return;
        }

        output.println("ONLINE_USERS " + String.join(
                ", ", onlineUsers.keySet()
        ));
    }
}