package gui;

import client.ServerConnection;
import java.awt.*;
import javax.swing.*;

public class MainFrame extends JFrame {

    private final ServerConnection connection;
    private final ChatPanel chatPanel;

    public MainFrame(String username, ServerConnection connection) {
        this.connection = connection;

        setTitle("MultiChat | " + username);
        setSize(900, 600);
        setMinimumSize(new Dimension(700, 450));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(24, 27, 33));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(32, 36, 44));
        header.setBorder(
                BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("MultiChat");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(111, 211, 156));

        JLabel userLabel = new JLabel("Logged in as: " + username);
        userLabel.setForeground(Color.WHITE);

        header.add(title, BorderLayout.WEST);
        header.add(userLabel, BorderLayout.EAST);

        chatPanel = new ChatPanel(username, connection);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(chatPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);

        // Receive server messages without freezing the GUI.
        Thread receiver = new Thread(() -> {
            String message;

            while ((message = connection.readResponse()) != null) {
                chatPanel.displayServerMessage(message);
            }

            SwingUtilities.invokeLater(() ->
                    chatPanel.displayServerMessage(
                            "Disconnected from server."));
        }, "MultiChat-Receiver");

        receiver.setDaemon(true);
        receiver.start();

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                connection.sendCommand("LOGOUT");
                connection.disconnect();
                dispose();
            }
        });
    }
}