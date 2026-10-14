package utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

public class MessageStore {

    private static final Path MESSAGE_DIR =
            Paths.get("data", "messages");

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static Path getConversationFile(
            String user1, String user2) {

        // Sort usernames so both users access the same file.
        String first;
        String second;

        if (user1.compareToIgnoreCase(user2) <= 0) {
            first = user1;
            second = user2;
        } else {
            first = user2;
            second = user1;
        }

        // Hex encoding avoids unsafe filename characters.
        String fileName = encode(first) + "_" + encode(second) + ".txt";

        return MESSAGE_DIR.resolve(fileName);
    }

    private static String encode(String username) {
        byte[] bytes = username.getBytes(StandardCharsets.UTF_8);
        StringBuilder result = new StringBuilder();

        for (byte b : bytes) {
            result.append(String.format("%02x", b & 0xff));
        }

        return result.toString();
    }

    public static synchronized void saveMessage(
            String sender, String receiver, String content) {

        try {
            Files.createDirectories(MESSAGE_DIR);

            Path file = getConversationFile(sender, receiver);

            String timestamp = LocalDateTime.now().format(FORMATTER);

            // Encode message content so newlines and separators are safe.
            String encodedContent = java.util.Base64.getEncoder()
                    .encodeToString(
                            content.getBytes(StandardCharsets.UTF_8));

            String record = timestamp + "\t"
                    + sender + "\t"
                    + receiver + "\t"
                    + encodedContent
                    + System.lineSeparator();

            Files.writeString(
                    file,
                    record,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);

        } catch (IOException e) {
            System.err.println(
                    "Unable to save message: " + e.getMessage());
        }
    }

    public static synchronized List<String> getHistory(
            String user1, String user2) {

        Path file = getConversationFile(user1, user2);

        try {
            if (!Files.exists(file)) {
                return List.of();
            }

            try (Stream<String> lines =
                         Files.lines(file, StandardCharsets.UTF_8)) {

                return lines.map(line -> {
                    try {
                        String[] parts = line.split("\t", 4);

                        if (parts.length != 4) {
                            return "[Unreadable message]";
                        }

                        String content = new String(
                                java.util.Base64.getDecoder()
                                        .decode(parts[3]),
                                StandardCharsets.UTF_8);

                        return "[" + parts[0] + "] "
                                + parts[1] + ": " + content;

                    } catch (IllegalArgumentException e) {
                        return "[Unreadable message]";
                    }
                }).toList();
            }

        } catch (IOException e) {
            System.err.println(
                    "Unable to load history: " + e.getMessage());
            return List.of();
        }
    }
}