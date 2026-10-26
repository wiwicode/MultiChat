
package gui;

import client.ServerConnection;
import java.awt.*;
import javax.swing.*;

public class LoginFrame extends JFrame {

    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JButton loginButton;
    private final JButton registerButton;
    private final JLabel statusLabel;

    public LoginFrame() {
        setTitle("MultiChat | Welcome");
        setSize(420, 520);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setBackground(new Color(24, 27, 33));
        panel.setBorder(
                BorderFactory.createEmptyBorder(35, 40, 35, 40));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("MultiChat");
        title.setFont(new Font("SansSerif", Font.BOLD, 34));
        title.setForeground(new Color(111, 211, 156));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle =
                new JLabel("Connect. Chat. Collaborate.");
        subtitle.setForeground(Color.LIGHT_GRAY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel usernameLabel = createLabel("Username");
        usernameField = new JTextField();
        styleField(usernameField);

        JLabel passwordLabel = createLabel("Password");
        passwordField = new JPasswordField();
        styleField(passwordField);

        loginButton = new JButton("Login");
        registerButton = new JButton("Create Account");

        styleButton(loginButton, true);
        styleButton(registerButton, false);

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(new Color(255, 120, 120));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginButton.addActionListener(e -> authenticate(false));
        registerButton.addActionListener(e -> authenticate(true));

        panel.add(title);
        panel.add(Box.createVerticalStrut(8));
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(35));
        panel.add(usernameLabel);
        panel.add(Box.createVerticalStrut(7));
        panel.add(usernameField);
        panel.add(Box.createVerticalStrut(20));
        panel.add(passwordLabel);
        panel.add(Box.createVerticalStrut(7));
        panel.add(passwordField);
        panel.add(Box.createVerticalStrut(25));
        panel.add(loginButton);
        panel.add(Box.createVerticalStrut(12));
        panel.add(registerButton);
        panel.add(Box.createVerticalStrut(15));
        panel.add(statusLabel);

        add(panel);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void styleField(JTextField field) {
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setPreferredSize(new Dimension(300, 40));
        field.setBackground(new Color(39, 43, 51));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);

        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(65, 72, 83)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        field.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void styleButton(JButton button, boolean primary) {
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        button.setPreferredSize(new Dimension(300, 42));
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (primary) {
            button.setBackground(new Color(111, 211, 156));
            button.setForeground(new Color(24, 27, 33));
        } else {
            button.setBackground(new Color(39, 43, 51));
            button.setForeground(Color.WHITE);
        }

        // Use the specified colours instead of the system defaults.
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private void authenticate(boolean register) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Enter username and password.");
            return;
        }

        loginButton.setEnabled(false);
        registerButton.setEnabled(false);
        statusLabel.setForeground(Color.LIGHT_GRAY);
        statusLabel.setText("Connecting to server...");

        new SwingWorker<String, Void>() {

            private ServerConnection connection;

            @Override
            protected String doInBackground() {
                connection = new ServerConnection();

                if (!connection.connect()) {
                    return "ERROR Could not connect to server.";
                }

                connection.sendCommand(
                        (register ? "REGISTER " : "LOGIN ")
                                + username + " " + password);

                String response = connection.readResponse();

                if (register || response == null
                        || !response.startsWith("LOGIN_SUCCESS")) {
                    connection.disconnect();
                }

                return response == null
                        ? "ERROR No response from server."
                        : response;
            }

            @Override
            protected void done() {
                try {
                    String response = get();

                    if (response.startsWith("REGISTER_SUCCESS")) {
                        statusLabel.setForeground(
                                new Color(111, 211, 156));
                        statusLabel.setText(
                                "Account created! Now log in.");

                    } else if (response.startsWith("LOGIN_SUCCESS")) {
                        // Transfer the authenticated connection.
                        ServerConnection authenticatedConnection =
                                connection;

                        dispose();

                        SwingUtilities.invokeLater(() ->
                                new MainFrame(
                                        username,
                                        authenticatedConnection
                                ).setVisible(true));

                        return;

                    } else {
                        statusLabel.setForeground(
                                new Color(255, 120, 120));
                        statusLabel.setText(
                                response.replaceFirst("^ERROR\\s*", ""));
                    }

                } catch (Exception ex) {
                    statusLabel.setForeground(
                            new Color(255, 120, 120));
                    statusLabel.setText("Login failed. Try again.");

                } finally {
                    loginButton.setEnabled(true);
                    registerButton.setEnabled(true);
                }
            }
        }.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
                // Continue with the default Look and Feel.
            }

            new LoginFrame().setVisible(true);
        });
    }
}
