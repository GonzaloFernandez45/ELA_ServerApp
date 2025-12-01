package UI;

import ReceiveData.ReceiveDataViaNetwork;
import ReceiveData.SendDataViaNetwork;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

/**
 * Console entry point for the ADMIN client.
 * Connects to the server, performs an initial role handshake, and
 * shows login / signup and admin menu options.
 */
public class AdminApp {

    /**
     * Starts the admin client:
     * - Asks for server IP (currently ignored, uses localhost).
     * - Connects to port 8888.
     * - Sends an initial code (3) to identify as ADMIN client.
     * - Waits for "ADMIN" response; if OK, shows admin login/signup menu.
     */
    public static void main(String[] args) {
        boolean running = true;
        Scanner scanner = new Scanner(System.in);

        // Connection loop (in case you want to retry)
        while (running) {
            String ipAddress = Utilities.readString("Enter the IP address of the server to connect to:\n");
            try {
                Socket socket = new Socket("localhost", 8888);
                SendDataViaNetwork sendDataViaNetwork = new SendDataViaNetwork(socket);
                ReceiveDataViaNetwork receiveDataViaNetwork = new ReceiveDataViaNetwork(socket);
                sendDataViaNetwork.sendInt(3);                  // Initial handshake: send code "3" to indicate ADMIN client

                // Expect server to respond with "ADMIN" if accepted
                String message = receiveDataViaNetwork.receiveString();
                System.out.println(message);

                if (message.equals("ADMIN")) {
                    // Proceed to admin login/signup menu
                    showAdminMenu(socket, sendDataViaNetwork, receiveDataViaNetwork);
                } else {
                    System.out.println("Server response invalid. Try again.");
                }
            } catch (IOException e) {
                System.out.println("Connection failed: " + e.getMessage());
                running = false;  // Salir si no se puede conectar
            }
        }
    }

    /**
     * Shows the first admin UI:
     * - 1: Log in
     * - 2: Sign up
     * - 0: Exit
     * Delegates the logic to AdminUI (logIn / register).
     *
     * @param socket                active socket connected to the server.
     * @param sendDataViaNetwork    helper for sending data.
     * @param receiveDataViaNetwork helper for receiving data.
     */
    public static void showAdminMenu(Socket socket, SendDataViaNetwork sendDataViaNetwork, ReceiveDataViaNetwork receiveDataViaNetwork) {
        boolean running = true;
        Scanner scanner = new Scanner(System.in);

        AdminUI  adminUi= new AdminUI();

        while (running) {
            System.out.println("1- Log in");
            System.out.println("2- Sign up");
            System.out.println("0- Exit");
            int option = scanner.nextInt();

            switch (option) {
                case 1:
                    try {
                        // Admin login flow
                        adminUi.logIn(socket,sendDataViaNetwork,receiveDataViaNetwork);  // Llama al método logIn() en la clase doctor
                    } catch (IOException e) {
                        System.out.println("Error during login: " + e.getMessage());
                    }
                    break;
                case 2:
                    try {
                        // Admin registration flow
                        adminUi.register(socket,sendDataViaNetwork,receiveDataViaNetwork);  // Llama al método register() en la clase doctor
                    } catch (IOException e) {
                        System.out.println("Error during registration: " + e.getMessage());
                    }
                    break;
                case 0:
                    System.out.println("Exiting...");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option, please try again.");
            }
        }
    }

    /**
     * Second-level admin menu after login.
     * Allows remote server control actions (e.g., stopping the server).
     *
     * Protocol:
     * - Reads user option (1 or 0).
     * - Sends the option to the server.
     * - For option 1, calls AdminUI.stopServerOption() to handle shutdown workflow.
     *
     * @param receiveDataViaNetwork helper for receiving data from server.
     * @param sendDataViaNetwork    helper for sending data to server.
     * @param socket                active socket to the server.
     * @throws IOException if network communication fails.
     */
    public static void menuAdmin(ReceiveDataViaNetwork receiveDataViaNetwork, SendDataViaNetwork sendDataViaNetwork, Socket socket) throws IOException {
        boolean running = true;
        // Scanner scanner = new Scanner(System.in); // MEJOR USA UTILITIES SI YA LA TIENES
        AdminUI adminUi = new AdminUI();

        while(running) {
            System.out.println("\n--- ADMIN MENU ---");
            System.out.println("1. Stop the Server");
            System.out.println("0. Exit");

            int option = Utilities.readInteger("Choose option: ");
            sendDataViaNetwork.sendInt(option);

            switch(option) {
                case 1:
                    adminUi.stopServerOption(sendDataViaNetwork, receiveDataViaNetwork);
                    break;

                case 0:
                    running = false;
                    break;

                default:
                    System.out.println("Invalid option, try again.");
            }
        }
    }
}
