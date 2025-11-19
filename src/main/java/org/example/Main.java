package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import interfaces.PatientManager;
import interfaces.UserManager;
import jdbc.*;
import ReceiveData.*;
import pojos.Patient;
import pojos.Role;
import pojos.User;
//prueba

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

        private static int activeClients = 0;
        /** The server's running state */
        private static boolean running = true;


        public static void main(String[] args) throws IOException {
            ConnectionManager manager = new ConnectionManager();
            ServerSocket serverSocket = new ServerSocket(8000);
            ConnectionManager conMan = new ConnectionManager();
            JDBCPatientManager patientManager = new JDBCPatientManager(conMan);
            JDBCUserManager userManager = new JDBCUserManager(conMan);
            Socket socket = serverSocket.accept();

            ReceiveDataViaNetwork recieveDataViaNetwork = null;
            SendDataViaNetwork sendDataViaNetwork = null;

                try {
                    recieveDataViaNetwork = new ReceiveDataViaNetwork(socket);
                    sendDataViaNetwork = new SendDataViaNetwork(socket);
                    System.out.println("Socket accepted");

                    int message = recieveDataViaNetwork.receiveInt();

                    if(message == 1){
                        sendDataViaNetwork.sendStrings("PATIENT");
                        patientMenu(sendDataViaNetwork, recieveDataViaNetwork, socket, patientManager, userManager); // atiende a este cliente y vuelve a escuchar
                    }

                } catch (IOException e) {
                    Logger.getLogger(Main.class.getName())
                            .log(Level.SEVERE, "Error handling client", e);
                    // Continúa el bucle: seguimos aceptando clientes
                } finally {
                    if (socket != null && !socket.isClosed()) {
                        try {
                            socket.close();
                        } catch (IOException ex) {
                            Logger.getLogger(Main.class.getName())
                                    .log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
            // No cerramos serverSocket para seguir aceptando clientes


    // Atiende a un cliente hasta 'x', EOF o desconexión abrupta.
    private static void handleClient() {
        System.out.println("Handling client...");


    }


    private static void patientMenu (SendDataViaNetwork sendDataViaNetwork, ReceiveDataViaNetwork recieveDataViaNetwork, Socket socket, PatientManager patientManager, JDBCUserManager userManager) throws IOException {
          try{
            boolean patientMenu = true;

            while(patientMenu){
                int opcion = recieveDataViaNetwork.receiveInt();
                switch(opcion){
                    case 1:
                        logInPatient(recieveDataViaNetwork, sendDataViaNetwork,socket,patientManager,userManager);
                        break;
                    case 2:
                        patientRegister(recieveDataViaNetwork,sendDataViaNetwork, socket, patientManager, userManager);
                        break;
                    case 3:
                        patientMenu = false;
                        System.out.println("Patient disconnected");
                        break;
                    default:
                        System.out.println("Invalid option");
                        break;
                }
            }
          }catch(IOException ex){
              System.out.println(ex.getMessage());
              releaseResources(recieveDataViaNetwork,sendDataViaNetwork, socket);
          }
    }

    private static void patientRegister(ReceiveDataViaNetwork recieveDataViaNetwork, SendDataViaNetwork sendDataViaNetwork, Socket socket, PatientManager patientManager, UserManager userManager) throws IOException {
            try{
                String message = recieveDataViaNetwork.receiveString();
                if(message.equals("OK")){
                    Patient patient = recieveDataViaNetwork.recievePatient();
                    User user = recieveDataViaNetwork.recieveUser();
                    System.out.println(user.toString());
                    int patient_id = patientManager.getPatientIDFromEmail(patient.getEmail());
                    user.setPatient_id(patient_id);
                    userManager.addUser(user);
                    patientManager.addPatient(patient);
                    sendDataViaNetwork.sendStrings("SUCCESS");
                    sendDataViaNetwork.sendPatient(patient);
                    patient.setId(patient_id);
                    menuPaciente(patient, sendDataViaNetwork, recieveDataViaNetwork, socket);


                }else{
                    System.out.println("Error in register");

                }
            }catch(IOException ex){
                System.out.println("Error or client disconnected");
                releaseResources(recieveDataViaNetwork,sendDataViaNetwork,socket);
            }
    }

    private static void menuPaciente(Patient patient, SendDataViaNetwork sendDataViaNetwork, ReceiveDataViaNetwork recieveDataViaNetwork, Socket socket) throws IOException {
        try{
            boolean patientMenu = true;

            while(patientMenu){
                int opcion = recieveDataViaNetwork.receiveInt();
                switch(opcion){
                    case 1:
                        System.out.println("1. Insert medical information");
                        break;
                    case 2:
                        System.out.println("2. Record Signal");
                        break;
                    case 3:
                        System.out.println("3. Send Signal");
                        break;
                    case 4:
                         System.out.println("4. See doctor´s feedback");
                         break;
                    case 0:
                        System.out.println("0. Exit");
                        break;
                    default:
                        System.out.println("Invalid option");
                        break;
                }
            }
        }catch(Exception ex){
            System.out.println(ex.getMessage());
            releaseResources(recieveDataViaNetwork,sendDataViaNetwork, socket);
        }
    }

    private static void logInPatient(ReceiveDataViaNetwork recieveDataViaNetwork, SendDataViaNetwork sendDataViaNetwork, Socket socket, PatientManager patientManager, UserManager userManager) throws IOException {
            try{
                sendDataViaNetwork.sendStrings("Patient log in");
                String message = recieveDataViaNetwork.receiveString();
                System.out.println(message);
                Role role = new Role("Patient");
                if(message.equals("OK")){
                    User user = recieveDataViaNetwork.recieveUser();
                    User u = userManager.checkPassword(new String(user.getPassword()), user.getEmail());
                    if(user != null && user.getRole().equals(role)){
                        sendDataViaNetwork.sendStrings("SUCCESS");
                        Patient patient = patientManager.getPatientFromUser(user);
                        int patient_id = patientManager.getPatientIDFromEmail(patient.getEmail());
                        user.setPatient_id(patient_id);
                        System.out.println(patient.toString());
                        sendDataViaNetwork.sendPatient(patient);
                        menuPaciente(patient, sendDataViaNetwork, recieveDataViaNetwork, socket);
                    }else{
                        sendDataViaNetwork.sendStrings("ERROR");}
                }else{
                    System.out.println("Error in login");
                }


            } catch (IOException e) {
                System.out.println("Error or client disconnected");
                releaseResources(recieveDataViaNetwork,sendDataViaNetwork,socket);
            }
    }
    private static void releaseResources(ReceiveDataViaNetwork recieveDataViaNetwork, SendDataViaNetwork sendDataViaNetwork, Socket socket){
        if(sendDataViaNetwork != null && recieveDataViaNetwork != null) {
            sendDataViaNetwork.releaseResources();
            recieveDataViaNetwork.releaseResources();
        }
        try {
            socket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}

