package UI;

import ReceiveData.ReceiveDataViaNetwork;
import ReceiveData.SendDataViaNetwork;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class AdminApp {

    public static void main(String[] args) {
        boolean running = true;
        Scanner scanner = new Scanner(System.in);

        // Establecer conexión con el servidor
        while (running) {
            String ipAddress = Utilities.readString("Enter the IP address of the server to connect to:\n");
            try {
                Socket socket = new Socket("localhost", 8888);
                SendDataViaNetwork sendDataViaNetwork = new SendDataViaNetwork(socket);
                ReceiveDataViaNetwork receiveDataViaNetwork = new ReceiveDataViaNetwork(socket);
                sendDataViaNetwork.sendInt(3);  // Se asume que se está enviando un código para verificar la conexión
                String message = receiveDataViaNetwork.receiveString();
                System.out.println(message);

                if (message.equals("ADMIN")) {
                    // Proceder con las opciones del paciente
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

    public static void showAdminMenu(Socket socket, SendDataViaNetwork sendDataViaNetwork, ReceiveDataViaNetwork receiveDataViaNetwork) {
        boolean running = true;
        Scanner scanner = new Scanner(System.in);

        // Crear un único objeto Patient que será usado en todos los casos
        AdminUI  adminUi= new AdminUI();

        while (running) {
            System.out.println("1- Log in");
            System.out.println("2- Sign up");
            System.out.println("0- Exit");
            int option = scanner.nextInt();

            switch (option) {
                case 1:
                    try {
                        adminUi.logIn(socket,sendDataViaNetwork,receiveDataViaNetwork);  // Llama al método logIn() en la clase doctor
                    } catch (IOException e) {
                        System.out.println("Error during login: " + e.getMessage());
                    }
                    break;
                case 2:
                    try {
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

    public static void menuAdmin(ReceiveDataViaNetwork receiveDataViaNetwork,SendDataViaNetwork sendDataViaNetwork, Socket socket) throws IOException {
        boolean running = true;
        Scanner scanner = new Scanner(System.in);
        AdminUI adminUi= new AdminUI();
        while(running) {
            System.out.println("Welcome to the Admin App!");
            System.out.println("Please choose an option");
            System.out.println("1.Stop the Server");
            System.out.println("0.Exit");
            int option = scanner.nextInt();
            sendDataViaNetwork.sendInt(option);

            switch(option) {
                case 1:
                    System.out.println("This should stop the server");
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
