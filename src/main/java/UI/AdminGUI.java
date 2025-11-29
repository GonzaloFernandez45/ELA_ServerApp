package UI;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class AdminGUI extends JFrame {

    private AdminClientContext context;

    private CardLayout cardLayout;
    private JPanel mainPanel;

    // Paneles
    private JPanel connectPanel; // <--- Nuevo panel
    private JPanel authPanel;
    private JPanel menuPanel;

    // Componentes de connectPanel
    private JTextField ipField;

    public AdminGUI() {
        super("Telemedicine - Administrator");
        // Nota: Ya no pedimos el 'context' en el constructor

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // 1. Crear los paneles
        connectPanel = createConnectPanel(); // <--- Creamos el panel de conexión
        authPanel = createAuthPanel();
        menuPanel = createMenuPanel();

        // 2. Añadirlos al CardLayout
        mainPanel.add(connectPanel, "CONNECT");
        mainPanel.add(authPanel, "AUTH");
        mainPanel.add(menuPanel, "MENU");

        setContentPane(mainPanel);

        // 3. Mostrar primero la pantalla de conexión
        cardLayout.show(mainPanel, "CONNECT");

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Ya no conectamos aquí, solo lanzamos la interfaz
            new AdminGUI();
        });
    }

    // ===================== PANTALLA 0: CONEXIÓN AL SERVIDOR =====================

    private JPanel createConnectPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Welcome to Telemedicine");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Arial", Font.BOLD, 22));

        JLabel ipLabel = new JLabel("Enter Server IP Address:");
        ipLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Campo de texto con "localhost" por defecto
        ipField = new JTextField("localhost");
        ipField.setMaximumSize(new Dimension(200, 30));
        ipField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton connectButton = new JButton("Connect");
        connectButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        connectButton.addActionListener(e -> attemptConnection());

        panel.add(Box.createVerticalStrut(50));
        panel.add(title);
        panel.add(Box.createVerticalStrut(40));
        panel.add(ipLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(ipField);
        panel.add(Box.createVerticalStrut(20));
        panel.add(connectButton);

        return panel;
    }

    private void attemptConnection() {
        String ip = ipField.getText().trim();
        if (ip.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an IP address.");
            return;
        }

        try {
            // Intentamos conectar creando el contexto
            // Asumimos puerto 8888 fijo, pero podrías poner otro campo para el puerto
            this.context = new AdminClientContext(ip, 8888);

            // Si no da error, pasamos a la siguiente pantalla
            JOptionPane.showMessageDialog(this, "Connected to server successfully!");
            cardLayout.show(mainPanel, "AUTH");

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error connecting to server at " + ip + ":\n" + e.getMessage(),
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===================== PANTALLA 1: LOGIN / REGISTER =====================

    private JPanel createAuthPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Admin Login");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Arial", Font.BOLD, 20));

        JButton loginButton = new JButton("Log in");
        JButton registerButton = new JButton("Register");

        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginButton.addActionListener(e -> showLoginForm());
        registerButton.addActionListener(e -> showRegisterForm());

        panel.add(Box.createVerticalStrut(40));
        panel.add(title);
        panel.add(Box.createVerticalStrut(40));
        panel.add(loginButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(registerButton);

        return panel;
    }

    private void showLoginForm() {
        JDialog dialog = new JDialog(this, "Log in", true);
        dialog.setSize(400, 200);
        dialog.setLayout(new GridLayout(3, 2));

        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        dialog.add(new JLabel("Email:"));
        dialog.add(emailField);
        dialog.add(new JLabel("Password:"));
        dialog.add(passwordField);

        JButton loginBtn = new JButton("Log in");
        dialog.add(new JLabel());
        dialog.add(loginBtn);

        loginBtn.addActionListener(ev -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            try {
                boolean ok = context.getAdminUI().logInFromGUI(
                        email,
                        password,
                        context.getSocket(),
                        context.getSendData(),
                        context.getReceiveData()
                );
                if (ok) {
                    JOptionPane.showMessageDialog(dialog, "Log in successful");
                    dialog.dispose();
                    cardLayout.show(dialog, "MENU");
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Incorrect user or password",
                            "Login error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Connection error: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showRegisterForm() {
        JDialog dialog = new JDialog(this, "Register", true);
        dialog.setSize(450, 350);
        dialog.setLayout(new GridLayout(8, 2));

        JTextField emailField = new JTextField();
        JTextField dniField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        dialog.add(new JLabel("Email:")); dialog.add(emailField);
        dialog.add(new JLabel("DNI:")); dialog.add(dniField);
        dialog.add(new JLabel("Password:")); dialog.add(passwordField);

        JButton registerBtn = new JButton("Register");
        dialog.add(new JLabel());
        dialog.add(registerBtn);

        registerBtn.addActionListener(ev -> {
            try {
                boolean ok = context.getAdminUI().registerFromGUI(
                        emailField.getText(),
                        dniField.getText(),
                        new String(passwordField.getPassword()),
                        context.getSocket(),
                        context.getSendData(),
                        context.getReceiveData()
                );
                if (ok) {
                    JOptionPane.showMessageDialog(dialog, "Admin registered successfully");
                    dialog.dispose();
                    cardLayout.show(mainPanel, "MENU");
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Registration failed",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Connection error: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }



    // ===================== PANTALLA : MENU 1 OPCION =====================

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Admin menu");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Arial", Font.BOLD, 18));

        JButton closeServerButton = new JButton("Close server");

        closeServerButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        //closeServerButton.addActionListener(e -> );


        panel.add(Box.createVerticalStrut(30));
        panel.add(title);

        return panel;
    }

}
