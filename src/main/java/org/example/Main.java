package org.example;

import ReceiveData.ClientHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

import interfaces.PatientManager;
import interfaces.SymptomManager;
import interfaces.UserManager;
import jdbc.*;
import ReceiveData.*;
import pojos.Patient;
import pojos.User;

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
                        patientMenu(sendDataViaNetwork, recieveDataViaNetwork, socket, patientManager); // atiende a este cliente y vuelve a escuchar
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


    private static void patientMenu( SendDataViaNetwork sendDataViaNetwork, ReceiveDataViaNetwork recieveDataViaNetwork,Socket socket, PatientManager patientManager){
          try{
            boolean patientMenu = true;

            while(patientMenu){
                int opcion = recieveDataViaNetwork.receiveInt();
                switch(opcion){
                    case 1:
                        break;
                    case 2:
                        patientRegister(recieveDataViaNetwork,sendDataViaNetwork, socket, patientManager);
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

    private static void patientRegister(ReceiveDataViaNetwork recieveDataViaNetwork, SendDataViaNetwork sendDataViaNetwork, Socket socket, PatientManager patientManager, UserManager userManager) {
            try{
                String message = recieveDataViaNetwork.receiveString();
                if(message.equals("OK")){
                    Patient patient = recieveDataViaNetwork.recievePatient();
                    User user = recieveDataViaNetwork.recieveUser();
                    System.out.println(user.toString());
                    //userManager.addUser...

                    patientManager.addPatient(patient);
                    sendDataViaNetwork.sendStrings("SUCCESS");
                    sendDataViaNetwork.sendPatient(patient);
                    //patient.setPatientId...
                    //menuPaciente(patient, sendDataViaNetwork, recieveDataViaNetwork, socket); CREAR


                }else{
                    System.out.println("Error in register");

                }
            }catch(IOException ex){
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

