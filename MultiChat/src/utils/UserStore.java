package utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class UserStore {

    private static final Path USERS_FILE =
            Paths.get("data", "users.txt");

    public static synchronized Map<String, String> loadUsers() {

        Map<String, String> users = new HashMap<>();

        try {

            if (!Files.exists(USERS_FILE)) {
                Files.createDirectories(USERS_FILE.getParent());
                Files.createFile(USERS_FILE);
                return users;
            }

            for (String line : Files.readAllLines(
                    USERS_FILE, StandardCharsets.UTF_8)) {

                String[] parts = line.split("\\t", 2);

                if (parts.length == 2) {
                    users.put(parts[0], parts[1]);
                }
            }

        } catch (IOException e) {
            System.out.println("Unable to read users: " + e.getMessage());
        }

        return users;
    }

    public static synchronized void saveUsers(
            Map<String, String> users) {

        StringBuilder data = new StringBuilder();

        for (Map.Entry<String, String> entry : users.entrySet()) {
            data.append(entry.getKey())
                .append('\t')
                .append(entry.getValue())
                .append(System.lineSeparator());
        }

        try {

            Files.createDirectories(USERS_FILE.getParent());

            Files.writeString(
                USERS_FILE,
                data.toString(),
                StandardCharsets.UTF_8
            );

        } catch (IOException e) {
            System.out.println("Unable to save users: " + e.getMessage());
        }
    }
}