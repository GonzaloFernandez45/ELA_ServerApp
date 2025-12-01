package UI;

import ReceiveData.ReceiveDataViaNetwork;
import ReceiveData.SendDataViaNetwork;
import pojos.Administrator;
import pojos.Doctor;
import pojos.Role;
import pojos.User;


import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.Socket;
import java.sql.Date;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Console + GUI helper for ADMIN actions.
 * Handles:
 * - Register / login from console.
 * - Register / login from Swing GUI.
 * - Server shutdown protocol (console + GUI).
 */
public class AdminUI {


    /**
     * Console-based admin registration flow.
     * Protocol:
     * - Send code 2 (register admin).
     * - Read email, dni, password from console.
     * - Build Administrator + User and send them to server.
     * - Wait for "SUCCESS" / other string from server.
     * If SUCCESS → open admin menu.
     */
    public void register(Socket socket, SendDataViaNetwork sendDataViaNetwork, ReceiveDataViaNetwork receiveDataViaNetwork) throws IOException {

        try {
            sendDataViaNetwork.sendInt(2); // Indicar al servidor que se va a registrar un admin

            Administrator administrator = new Administrator();
            Role role = new Role("Administrator");

            String email = Utilities.readString("Enter your email: ");
            administrator.setEmail(email);

            String dni = Utilities.readString("Enter your dni: ");
            administrator.setDni(dni);

            String password = Utilities.readString("Enter your password: ");
            byte[] passwordBytes = password.getBytes(); // Convertir la contraseña a bytes

            if (passwordBytes != null) {
                sendDataViaNetwork.sendStrings("OK");
                User user = new User(email, passwordBytes, role);
                System.out.println(administrator);
                System.out.println(user);
                sendDataViaNetwork.sendAdmin(administrator);
                sendDataViaNetwork.sendUser(user);

                if (receiveDataViaNetwork.receiveString().equals("SUCCESS")) {
                    System.out.println("Admin registered successfully.");
                    AdminApp.menuAdmin(receiveDataViaNetwork,sendDataViaNetwork,socket);

                } else {
                    System.out.println("Registration failed. Please try again.");
                    return; // Salir del metodo si el registro falla
                }
            } else {
                sendDataViaNetwork.sendStrings("ERROR");
            }

        } catch (IOException e) {
            System.out.println("Error in connection");
            releaseResources(socket, sendDataViaNetwork, receiveDataViaNetwork);
            System.exit(0);
        }
    }

    /**
     * Console-based admin login flow.
     * Protocol:
     * - Send code 1 (login).
     * - Server sends initial message (printed).
     * - Read email + password from console.
     * - Build User and send to server.
     * - Read "SUCCESS" / "ERROR" / other string.
     * If SUCCESS → receive Administrator object and open admin menu.
     */
    public void logIn(Socket socket, SendDataViaNetwork sendDataViaNetwork, ReceiveDataViaNetwork receiveDataViaNetwork) throws IOException {
        try {
            sendDataViaNetwork.sendInt(1);
            System.out.println(receiveDataViaNetwork.receiveString());

            String username = Utilities.readString("Enter your email: ");

            String password = Utilities.readString("Enter your password: ");

            byte[] passwordBytes = password.getBytes();

            Role role = new Role("Doctor");

            if(passwordBytes != null) {
                sendDataViaNetwork.sendStrings("OK");
                User user = new User(username, passwordBytes, role);
                sendDataViaNetwork.sendUser(user);
                String response = receiveDataViaNetwork.receiveString();
                System.out.println(response);

                if(response.equals("SUCCESS")) {
                    try{
                        Administrator administrator = receiveDataViaNetwork.recieveAdmin();
                        System.out.println(administrator.toString());
                        if (administrator != null) {
                            System.out.println("Log in successful");
                            AdminApp.menuAdmin(receiveDataViaNetwork,sendDataViaNetwork,socket);

                        } else {
                            System.out.println("Doctor not found");
                        }
                    } catch (IOException e) {
                        System.out.println("Log in problem");
                    }
                } else if (response.equals("ERROR")) {
                    System.out.println("User or password is incorrect");
                } else {
                    System.out.println("Login failed. Please check your credentials.");
                }


            }else {
                sendDataViaNetwork.sendStrings("ERROR");
            }

        }catch(IOException e){
            System.out.println("Error in connection");
            releaseResources(socket, sendDataViaNetwork,receiveDataViaNetwork);
            System.exit(0);
        }
    }

    /**
     * Utility method to close network resources for console flows.
     */
    private static void releaseResources(Socket socket, SendDataViaNetwork sendDataViaNetwork, ReceiveDataViaNetwork receiveDataViaNetwork) {
        if (sendDataViaNetwork != null && receiveDataViaNetwork != null) {
            sendDataViaNetwork.releaseResources();
            receiveDataViaNetwork.releaseResources();
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(AdminApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * GUI login flow.
     * Used by AdminGUI:
     * - Sends login code (1).
     * - Reads and prints initial server message.
     * - Sends "OK" + User (Administrator role).
     * - Waits for "SUCCESS".
     * - If success → receives Administrator and returns true.
     *
     * @return true if login OK, false otherwise.
     */
    public boolean logInFromGUI(
            String username,
            String password,
            Socket socket,
            SendDataViaNetwork sendDataViaNetwork,
            ReceiveDataViaNetwork receiveDataViaNetwork) throws IOException {

        sendDataViaNetwork.sendInt(1); // login

        // mensaje inicial del servidor, lo leemos y lo ignoramos o mostramos en consola
        String serverMsg = receiveDataViaNetwork.receiveString();
        System.out.println("Server says: " + serverMsg);

        byte[] passwordBytes = password.getBytes();
        Role role = new Role("Administrator");
        User user = new User(username, passwordBytes, role);

        sendDataViaNetwork.sendStrings("OK");
        sendDataViaNetwork.sendUser(user);

        String response = receiveDataViaNetwork.receiveString(); // "SUCCESS" o "ERROR"
        if (!response.equals("SUCCESS")) {
            return false;
        }

        Administrator administrator = receiveDataViaNetwork.recieveAdmin();
        System.out.println("Admin logged in: " + administrator);
        return administrator != null;
    }

    /**
     * GUI registration flow.
     * Used by AdminGUI:
     * - Sends code 2 (register admin).
     * - Builds Administrator + User from GUI fields.
     * - Sends "OK" + admin + user.
     * - Waits for "SUCCESS" / "ERROR".
     *
     * @return true if registration succeeded.
     */
    public boolean registerFromGUI(
            String email,
            String dni,
            String password,
            Socket socket,
            SendDataViaNetwork sendDataViaNetwork,
            ReceiveDataViaNetwork receiveDataViaNetwork) throws IOException {

        sendDataViaNetwork.sendInt(2); // registrar doctor

        Administrator administrator = new Administrator();

        administrator.setEmail(email);
        administrator.setDni(dni);

        byte[] passwordBytes = password.getBytes();
        Role role = new Role("Administrator");
        User user = new User(email, passwordBytes, role);

        sendDataViaNetwork.sendStrings("OK");
        sendDataViaNetwork.sendAdmin(administrator);
        sendDataViaNetwork.sendUser(user);

        String response = receiveDataViaNetwork.receiveString(); // "SUCCESS" o "ERROR"
        return response.equals("SUCCESS");
    }

    /**
     * Console-based server shutdown flow.
     * Protocol:
     * - Receive first string (WARNING or PASSWORD_REQUIRED).
     * - If WARNING: print, ask console "yes/no", send, then read next response.
     *      - If cancelled → print and exit.
     * - If PASSWORD_REQUIRED: ask for password, send it, read result:
     *      - If SHUTDOWN_OK → exit application.
     *      - Else → print error.
     */

    public void stopServerOption(SendDataViaNetwork sendData, ReceiveDataViaNetwork receiveData) throws IOException {
        // 1. Recibir primer mensaje (puede ser WARNING o PASSWORD_REQUIRED)
        String serverResponse = receiveData.receiveString();

        // 2. Caso WARNING
        if (serverResponse.startsWith("WARNING")) {
            System.out.println("SERVER: " + serverResponse);

            // Leemos lo que escribes (fghj, sdg... hasta que des enter)
            String confirm = Utilities.readString("Type 'yes' to force shutdown: ");
            sendData.sendStrings(confirm);

            // Esperamos la siguiente respuesta del servidor
            // Puede ser "Shutdown cancelled..." o "PASSWORD_REQUIRED"
            serverResponse = receiveData.receiveString();

            // Si el servidor dice que se canceló, imprimimos y salimos
            if (serverResponse.contains("cancelled")) {
                System.out.println("SERVER: " + serverResponse);
                return;
            }
        }

        // 3. Caso CONTRASEÑA (PASSWORD_REQUIRED)
        // Llegamos aquí si no hubo warning O si dijimos "yes" al warning
        if (serverResponse.equals("PASSWORD_REQUIRED")) {
            String pass = Utilities.readString("Enter admin password to shut down: ");
            sendData.sendStrings(pass);

            String result = receiveData.receiveString();
            if (result.equals("SHUTDOWN_OK")) {
                System.out.println("Server shutting down successfully. Closing Admin App.");
                System.exit(0);
            } else {
                System.out.println("Error: " + result);
            }
        } else {
            // Por si acaso llega otra cosa inesperada
            System.out.println("Server message: " + serverResponse);
        }
    }

    /**
     * GUI-based server shutdown flow.
     * Similar protocol as stopServerOption but using JOptionPane:
     * - If WARNING: show confirm dialog (YES/NO), send "yes"/"no".
     * - If cancelled → show info and return.
     * - If PASSWORD_REQUIRED: show password dialog, send password, handle result.
     * - If SHUTDOWN_OK → confirm and exit app.
     * - Otherwise show error or generic server message.
     */
    public void stopServerOptionGUI(SendDataViaNetwork sendData,
                                    ReceiveDataViaNetwork receiveData,
                                    Component parent) {
        try {
            // 1. Recibir primer mensaje (WARNING / PASSWORD_REQUIRED / lo que sea)
            String serverResponse = receiveData.receiveString();

            // 2. Caso WARNING
            if (serverResponse.startsWith("WARNING")) {

                int choice = JOptionPane.showConfirmDialog(
                        parent,
                        serverResponse + "\n\nDo you really want to force shutdown?",
                        "Server warning",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                String confirm = (choice == JOptionPane.YES_OPTION) ? "yes" : "no";
                sendData.sendStrings(confirm);

                // Esperar la siguiente respuesta del servidor
                serverResponse = receiveData.receiveString();

                // Si el servidor dice que se ha cancelado, mostramos y salimos
                if (serverResponse.contains("Shutdown cancelled")) {
                    JOptionPane.showMessageDialog(
                            parent,
                            serverResponse,
                            "Shutdown cancelled",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    return;
                }
            }

            // 3. Caso CONTRASEÑA
            if (serverResponse.equals("PASSWORD_REQUIRED")) {

                // Cuadro de diálogo para introducir contraseña
                JPasswordField passField = new JPasswordField();
                int res = JOptionPane.showConfirmDialog(
                        parent,
                        passField,
                        "Enter admin password to shut down",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                if (res != JOptionPane.OK_OPTION) {
                    // Usuario canceló → mandamos algo vacío (o nada) y salimos
                    sendData.sendStrings("");
                    JOptionPane.showMessageDialog(
                            parent,
                            "Shutdown cancelled by user.",
                            "Cancelled",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    return;
                }

                String pass = new String(passField.getPassword());
                sendData.sendStrings(pass);

                String result = receiveData.receiveString();

                if (result.equals("SHUTDOWN_OK")) {
                    JOptionPane.showMessageDialog(
                            parent,
                            "Server shutting down successfully. Closing Admin App.",
                            "Shutdown",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    System.exit(0);
                } else {
                    JOptionPane.showMessageDialog(
                            parent,
                            "Error: " + result,
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }

            } else {
                // Por si llega otra cosa
                JOptionPane.showMessageDialog(
                        parent,
                        "Server message: " + serverResponse,
                        "Server",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    parent,
                    "Connection error: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }


}
