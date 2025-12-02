package UI;

import ReceiveData.ReceiveDataViaNetwork;
import ReceiveData.SendDataViaNetwork;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Swing GUI client for ADMIN users.
 * Handles:
 * - Connection screen (enter IP, connect, handshake as ADMIN).
 * - Auth screen (login / register).
 * - Admin menu (e.g. close server).
 */
public class AdminGUI extends JFrame {

    private AdminClientContext context;

    private CardLayout cardLayout;
    private JPanel mainPanel;

    // Screens
    private JPanel connectPanel;
    private JPanel authPanel;
    private JPanel menuPanel;

    // Connect panel components
    private JTextField ipField;

    // Connect panel components
    private static final Color BG_COLOR = new Color(238, 244, 255);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(210, 220, 240);
    private static final Color TEXT_DARK = new Color(30, 30, 30);
    private static final Color BLUE_BUTTON = new Color(86, 132, 225);
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 26);
    private static final Font SUBTITLE_FONT = new Font("SansSerif", Font.BOLD, 20);
    private static final Font BUTTON_FONT = new Font("SansSerif", Font.PLAIN, 15);

    /**
     * Builds the main admin window:
     * - Sets Nimbus L&F.
     * - Creates card layout with 3 screens: CONNECT, AUTH, MENU.
     */
    public AdminGUI() {
        super("Telemedicine - Administrator");

        // Look&Feel Nimbus
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setMinimumSize(new Dimension(900, 600));

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(BG_COLOR);

        connectPanel = createConnectPanel();
        authPanel = createAuthPanel();
        menuPanel = createMenuPanel();

        mainPanel.add(connectPanel, "CONNECT");
        mainPanel.add(authPanel, "AUTH");
        mainPanel.add(menuPanel, "MENU");

        setContentPane(mainPanel);
        cardLayout.show(mainPanel, "CONNECT");
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Launches the Admin GUI on the EDT.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdminGUI::new);
    }

    // ===== Helper UI =====

    /**
     * Applies card-like style (border + padding) to a panel.
     */
    private void styleCard(JPanel panel) {
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
    }

    /**
     * Styles a button for use in menus.
     */
    private void styleMenuButton(JButton button) {
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setBackground(new Color(245, 248, 255));
        button.setForeground(TEXT_DARK);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

// ===== CONNECT screen =====

    /**
     * Creates the first screen:
     * - IP input.
     * - "Connect" button → attemptConnection().
     */
    private JPanel createConnectPanel() {
        JPanel bg = new JPanel(new GridBagLayout());
        bg.setBackground(BG_COLOR);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        styleCard(panel);

        JLabel title = new JLabel("Welcome to Telemedicine");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(TITLE_FONT);

        JLabel ipLabel = new JLabel("Enter Server IP Address:");
        ipLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        ipLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        ipField = new JTextField("localhost", 18);
        ipField.setMaximumSize(new Dimension(260, 32));
        ipField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton connectButton = new JButton("Connect");
        connectButton.setBackground(BLUE_BUTTON);
        connectButton.setForeground(Color.WHITE);
        connectButton.setFont(BUTTON_FONT);
        connectButton.setFocusPainted(false);
        connectButton.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        connectButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        connectButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        connectButton.addActionListener(e -> attemptConnection());

        panel.add(Box.createVerticalStrut(10));
        panel.add(title);
        panel.add(Box.createVerticalStrut(25));
        panel.add(ipLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(ipField);
        panel.add(Box.createVerticalStrut(20));
        panel.add(connectButton);
        panel.add(Box.createVerticalStrut(10));

        bg.add(panel);
        return bg;
    }

    /**
     * Tries to create an AdminClientContext to the given IP.
     * On success → show AUTH screen.
     * On failure → show error dialog.
     */
    private void attemptConnection() {
        String ip = ipField.getText().trim();
        if (ip.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an IP address.");
            return;
        }

        try {
            this.context = new AdminClientContext(ip, 8888);
            JOptionPane.showMessageDialog(this, "Connected successfully!");
            cardLayout.show(mainPanel, "AUTH");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error connecting: " + e.getMessage(),
                    "Connection error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

// ===== AUTH screen (login / register) =====

    /**
     * Screen with buttons to log in or register as admin.
     */

    private JPanel createAuthPanel() {
        JPanel bg = new JPanel(new GridBagLayout());
        bg.setBackground(BG_COLOR);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        styleCard(panel);
        panel.setPreferredSize(new Dimension(380, 260));

        JLabel title = new JLabel("Admin Login");
        title.setFont(SUBTITLE_FONT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton loginButton = new JButton("Log in");
        JButton registerButton = new JButton("Register");

        styleMenuButton(loginButton);
        styleMenuButton(registerButton);

        loginButton.addActionListener(e -> showLoginForm());
        registerButton.addActionListener(e -> showRegisterForm());

        panel.add(Box.createVerticalStrut(15));
        panel.add(title);
        panel.add(Box.createVerticalStrut(25));
        panel.add(loginButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(registerButton);
        panel.add(Box.createVerticalStrut(15));

        bg.add(panel);
        return bg;
    }

    /**
     * Modal dialog for admin login.
     * Calls AdminUI.logInFromGUI() and if OK → go to MENU screen.
     */


    private void showLoginForm() {
        JDialog dialog = new JDialog(this, "Log in Admin", true);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(CARD_COLOR);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(CARD_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4,8,4,8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField emailField = new JTextField(18);
        JPasswordField passwordField = new JPasswordField(18);

        gbc.gridx=0; gbc.gridy=0; content.add(new JLabel("Email:"), gbc);
        gbc.gridx=1; content.add(emailField, gbc);

        gbc.gridx=0; gbc.gridy=1; content.add(new JLabel("Password:"), gbc);
        gbc.gridx=1; content.add(passwordField, gbc);

        JButton loginBtn = new JButton("Log in");
        // Tus estilos de botón de Admin
        loginBtn.setBackground(new Color(70, 130, 180));
        loginBtn.setForeground(Color.WHITE);

        gbc.gridy=2; gbc.gridx=0; gbc.gridwidth=2;
        gbc.anchor = GridBagConstraints.CENTER;
        content.add(loginBtn, gbc);

        loginBtn.addActionListener(ev -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());

            loginBtn.setEnabled(false);
            loginBtn.setText("Connecting...");

            new Thread(() -> {
                try {
                    boolean ok = context.getAdminUI().logInFromGUI(
                            email,
                            password,
                            context.getSocket(),
                            context.getSendData(),
                            context.getReceiveData()
                    );

                    SwingUtilities.invokeLater(() -> {
                        loginBtn.setEnabled(true);
                        loginBtn.setText("Log in");

                        if (ok) {
                            JOptionPane.showMessageDialog(dialog, "Login successful");
                            dialog.dispose();
                            cardLayout.show(mainPanel, "MENU"); // Pantalla del Admin
                        } else {
                            JOptionPane.showMessageDialog(dialog, "Incorrect user or password", "Login error", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                } catch (IOException ex) {
                    SwingUtilities.invokeLater(() -> {
                        loginBtn.setEnabled(true);
                        loginBtn.setText("Log in");
                        JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
                    });
                }
            }).start();
        });

        dialog.getContentPane().add(content);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Modal dialog for admin registration.
     * Calls AdminUI.registerFromGUI(). If OK → go to MENU screen.
     */

    private void showRegisterForm() {
        JDialog dialog = new JDialog(this, "Register Admin", true);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(CARD_COLOR);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(CARD_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3,8,3,8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField emailField = new JTextField(18);
        JTextField dniField = new JTextField(12);
        JPasswordField passwordField = new JPasswordField(18);

        gbc.gridx=0; gbc.gridy=0; content.add(new JLabel("Email:"), gbc);
        gbc.gridx=1; content.add(emailField, gbc);

        gbc.gridx=0; gbc.gridy=1; content.add(new JLabel("DNI:"), gbc);
        gbc.gridx=1; content.add(dniField, gbc);

        gbc.gridx=0; gbc.gridy=2; content.add(new JLabel("Password:"), gbc);
        gbc.gridx=1; content.add(passwordField, gbc);

        JButton registerBtn = new JButton("Register");
        registerBtn.setBackground(new Color(70, 130, 180));
        registerBtn.setForeground(Color.WHITE);

        gbc.gridy=3; gbc.gridx=0; gbc.gridwidth=2;
        gbc.anchor = GridBagConstraints.CENTER;
        content.add(registerBtn, gbc);

        registerBtn.addActionListener(ev -> {
            String email = emailField.getText();
            String dni = dniField.getText();
            String password = new String(passwordField.getPassword());

            registerBtn.setEnabled(false);
            registerBtn.setText("Registering...");

            new Thread(() -> {
                try {
                    boolean ok = context.getAdminUI().registerFromGUI(
                            email, dni, password,
                            context.getSocket(), context.getSendData(), context.getReceiveData()
                    );

                    SwingUtilities.invokeLater(() -> {
                        registerBtn.setEnabled(true);
                        registerBtn.setText("Register");

                        if (ok) {
                            JOptionPane.showMessageDialog(dialog, "Registered successfully");
                            dialog.dispose();

                            // === RECONEXIÓN ===
                            try {
                                context.getSocket().close();

                                String ip = (ipField != null) ? ipField.getText().trim() : "localhost";
                                if(ip.isEmpty()) ip = "localhost";

                                // Reiniciamos contexto Admin
                                context = new AdminClientContext(ip, 9000); // O tu puerto de Admin

                                showLoginForm();

                            } catch (IOException e) {
                                JOptionPane.showMessageDialog(mainPanel, "Registered but failed to reconnect: " + e.getMessage());
                                cardLayout.show(mainPanel, "AUTH");
                            }
                            // ==================

                        } else {
                            JOptionPane.showMessageDialog(dialog, "Registration failed", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                } catch (IOException ex) {
                    SwingUtilities.invokeLater(() -> {
                        registerBtn.setEnabled(true);
                        registerBtn.setText("Register");
                        JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
                    });
                }
            }).start();
        });

        dialog.getContentPane().add(content);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }


    // ===== MENU screen =====

    /**
     * Creates the admin menu screen with actions (currently: close server).
     */

    private JPanel createMenuPanel() {
        JPanel bg = new JPanel(new GridBagLayout());
        bg.setBackground(BG_COLOR);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        styleCard(panel);

        JLabel title = new JLabel("Admin Menu");
        title.setFont(SUBTITLE_FONT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton closeServerButton = new JButton("Close server");
        styleMenuButton(closeServerButton);

        // **Pendiente** implementar acción
        closeServerButton.addActionListener(e -> onStopServerButton());

        panel.add(Box.createVerticalStrut(15));
        panel.add(title);
        panel.add(Box.createVerticalStrut(20));
        panel.add(closeServerButton);
        panel.add(Box.createVerticalStrut(15));

        bg.add(panel);
        return bg;
    }

    /**
     * Sends a "stop server" command in a background thread and
     * delegates the protocol details to AdminUI.stopServerOptionGUI().
     */
    private void onStopServerButton() {
        if (context == null) {
            JOptionPane.showMessageDialog(this,
                    "You are not connected to the server.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        new Thread(() -> {
            try {
                SendDataViaNetwork sendData = context.getSendData();
                ReceiveDataViaNetwork receiveData = context.getReceiveData();

                // 1. Send option 1 to server's admin menu (STOP SERVER)
                sendData.sendInt(1);  // STOP SERVER

                // 2. Run shutdown handshake using AdminUI helper
                context.getAdminUI().stopServerOptionGUI(
                        sendData,
                        receiveData,
                        AdminGUI.this   // parent for the JOptionPane
                );

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                        AdminGUI.this,
                        "Error stopping server: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                ));
            }
        }).start();
    }


}
