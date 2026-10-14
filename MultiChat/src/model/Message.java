package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {

    private String sender;
    private String receiver;
    private String content;
    private LocalDateTime timestamp;

    // Constructor
    public Message(String sender, String receiver, String content) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // Format the message for display
    public String toString() {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("HH:mm:ss");

        return "[" + timestamp.format(formatter) + "] "
                + sender + ": " + content;
    }
}