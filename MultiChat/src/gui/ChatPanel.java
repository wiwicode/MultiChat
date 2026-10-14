package gui;

import client.ServerConnection;
import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;

public class ChatPanel extends JPanel {

    private final ServerConnection connection;
    private final String username;

    private final JTextArea chatArea;
    private final JTextField recipientField;
    private final JTextField messageField;
    private final JButton sendButton;
    private final JButton usersButton;
    private final JLabel statusLabel;

    public ChatPanel(String username, ServerConnection connection) {
        this.username = username;
        this.connection = connection;

        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(24, 27, 33));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Chat history
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        chatArea.setBackground(new Color(32, 36, 44));
        chatArea.setForeground(Color.WHITE);
        chatArea.setCaretColor(Color.WHITE);
        chatArea.setMargin(new Insets(12, 12, 12, 12));

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Recipient selection
        JPanel recipientPanel = new JPanel(new BorderLayout(8, 0));
        recipientPanel.setOpaque(false);

        JLabel recipientLabel = new JLabel("Send privately to:");
        recipientLabel.setForeground(Color.WHITE);

        recipientField = new JTextField();
        styleField(recipientField);
        recipientField.setToolTipText("Enter the recipient's username");

        usersButton = new JButton("Online Users");
        styleButton(usersButton);

        recipientPanel.add(recipientLabel, BorderLayout.WEST);
        recipientPanel.add(recipientField, BorderLayout.CENTER);
        recipientPanel.add(usersButton, BorderLayout.EAST);

        // Message input
        JPanel inputPanel = new JPanel(new BorderLayout(8, 0));
        inputPanel.setOpaque(false);

        messageField = new JTextField();
        styleField(messageField);
        messageField.setToolTipText("Write your message");

        sendButton = new JButton("Send");
        styleButton(sendButton);

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        // Status
        statusLabel = new JLabel("Logged in as " + username);
        statusLabel.setForeground(new Color(111, 211, 156));

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 8));
        bottomPanel.setOpaque(false);
        bottomPanel.add(inputPanel, BorderLayout.CENTER);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);

        add(recipientPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(this::sendMessage);
        messageField.addActionListener(this::sendMessage);
        usersButton.addActionListener(e -> requestOnlineUsers());

        appendMessage("Welcome to MultiChat, " + username + "!");
        appendMessage("Enter a recipient username to send a private message.");
    }

    private void sendMessage(ActionEvent event) {
        String recipient = recipientField.getText().trim();
        String message = messageField.getText().trim();

        if (recipient.isEmpty()) {
            statusLabel.setText("Enter the recipient's username.");
            return;
        }

        if (message.isEmpty()) {
            return;
        }

        if (recipient.equals(username)) {
            statusLabel.setText("You cannot message yourself.");
            return;
        }

        // The server protocol splits commands into at most three parts.
        // Therefore, spaces in the message are supported.
        connection.sendCommand("MSG " + recipient + " " + message);

        messageField.setText("");
        messageField.requestFocusInWindow();
    }

    private void requestOnlineUsers() {
    statusLabel.setText("Fetching online users...");
    connection.sendCommand("USERS");
}
    

    public void displayServerMessage(String message) {
    SwingUtilities.invokeLater(() -> {

        if (message.startsWith("ONLINE_USERS ")) {
            String users = message.substring("ONLINE_USERS ".length());

            appendMessage("Online users: " + users);
            statusLabel.setText("Online users refreshed");

        } else if (message.startsWith("ERROR ")) {
            statusLabel.setText(
                    message.substring("ERROR ".length()));

        } else {
            appendMessage(message);
        }
    });
}

    private void appendMessage(String message) {
        chatArea.append(message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    private void styleField(JTextField field) {
        field.setBackground(new Color(39, 43, 51));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(65, 72, 83)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(111, 211, 156));
        button.setForeground(new Color(24, 27, 33));
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}