package UI;

import ReceiveData.ReceiveDataViaNetwork;
import ReceiveData.SendDataViaNetwork;
import pojos.Administrator;
import pojos.Role;
import pojos.User;


import java.io.IOException;
import java.net.Socket;
import java.sql.Date;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminUI {



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

}
