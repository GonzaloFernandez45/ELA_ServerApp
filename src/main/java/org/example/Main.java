package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import interfaces.MedicalInformationManager;
import interfaces.PatientManager;
import interfaces.SymptomManager;
import interfaces.UserManager;
import jdbc.*;
import ReceiveData.*;
import pojos.*;
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


    private static void patientMenu(SendDataViaNetwork sendDataViaNetwork, ReceiveDataViaNetwork recieveDataViaNetwork, Socket socket, PatientManager patientManager, JDBCUserManager userManager) throws IOException {
          try{
            boolean patientMenu = true;

            while(patientMenu){
                int opcion = recieveDataViaNetwork.receiveInt();
                switch(opcion){
                    case 1:
                        logInPatient(recieveDataViaNetwork, sendDataViaNetwork,socket,patientManager,userManager);
                        break;
                    case 2:
                        System.out.println("Patient register");
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
                System.out.println(message);

                if(message.equals("OK")){
                    System.out.println("Registering patient...");
                    Patient patient = recieveDataViaNetwork.recievePatient();
                    System.out.println(patient.toString());
                    User user = recieveDataViaNetwork.recieveUser();
                    System.out.println(user.toString());
                    patientManager.addPatient(patient); //Primero añadir al paciente a la DB

                    int patient_id = patientManager.getPatientIDFromEmail(patient.getEmail()); //Obtener su ID

                    user.setPatient_id(patient_id); //Asignar la Foreign Key al usuario
                    userManager.addUser(user); //Añadir el usuario a la DB

                    sendDataViaNetwork.sendStrings("SUCCESS");

                    patient.setId(patient_id);
                    menuPaciente(patient,sendDataViaNetwork, recieveDataViaNetwork, socket);

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
                        //patientInsertMedicalInformartion(recieveDataViaNetwork, sendDataViaNetwork, socket, patientManager, symptomManager);
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
                    boolean correctPassword = userManager.checkPassword(new String(user.getPassword()), user.getEmail());
                    if(correctPassword){
                        sendDataViaNetwork.sendStrings("SUCCESS");

                        int patient_id = patientManager.getPatientIDFromEmail(user.getEmail());
                        Patient patient = patientManager.getPatientbyId(patient_id);
                        user.setPatient_id(patient_id);

                        System.out.println(patient.toString());

                        sendDataViaNetwork.sendPatient(patient);
                        //
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

    public static void addFeedback (Socket socket) throws IOException {
            ReceiveDataViaNetwork receiveDataViaNetwork = new ReceiveDataViaNetwork(socket);
            int patientId = receiveDataViaNetwork.receiveInt();

            String feedback = receiveDataViaNetwork.receiveString();

            JDBCPatientManager patientManager = new JDBCPatientManager(new ConnectionManager());
            String responseMessage = patientManager.addFeedback(patientId,feedback);

            SendDataViaNetwork sendData = new SendDataViaNetwork(socket);
            sendData.sendStrings(responseMessage);

    }
    public static void updatePatientName(Socket socket) throws IOException {
        // Recibir el ID del paciente
        ReceiveDataViaNetwork receiveData = new ReceiveDataViaNetwork(socket);
        int patientId = receiveData.receiveInt();

        // Recibir el nuevo nombre
        String newName = receiveData.receiveString();

        // Crear una instancia de JDBCPatientManager
        JDBCPatientManager patientManager = new JDBCPatientManager(new ConnectionManager());

        // Llamar al método de JDBCPatientManager para actualizar el nombre
        boolean success = patientManager.updatePatientName(patientId, newName);

        // Enviar respuesta al doctor
        SendDataViaNetwork sendData = new SendDataViaNetwork(socket);
        if (success) {
            sendData.sendStrings("Patient name updated successfully.");
        } else {
            sendData.sendStrings("Error updating patient name.");
        }
    }

    public static void updatePatientSurname(Socket socket) throws IOException {
        // Recibir el ID del paciente
        ReceiveDataViaNetwork receiveData = new ReceiveDataViaNetwork(socket);
        int patientId = receiveData.receiveInt();

        // Recibir el nuevo nombre
        String newSurname = receiveData.receiveString();

        // Crear una instancia de JDBCPatientManager
        JDBCPatientManager patientManager = new JDBCPatientManager(new ConnectionManager());

        // Llamar al método de JDBCPatientManager para actualizar el nombre
        boolean success = patientManager.updatePatientSurname(patientId, newSurname);

        // Enviar respuesta al doctor
        SendDataViaNetwork sendData = new SendDataViaNetwork(socket);
        if (success) {
            sendData.sendStrings("Patient surname updated successfully.");
        } else {
            sendData.sendStrings("Error updating patient name.");
        }
    }
    public static void updatePatientDNI(Socket socket) throws IOException {
        // Recibir el ID del paciente
        ReceiveDataViaNetwork receiveData = new ReceiveDataViaNetwork(socket);
        int patientId = receiveData.receiveInt();

        // Recibir el nuevo nombre
        String newDni = receiveData.receiveString();

        // Crear una instancia de JDBCPatientManager
        JDBCPatientManager patientManager = new JDBCPatientManager(new ConnectionManager());

        // Llamar al método de JDBCPatientManager para actualizar el nombre
        boolean success = patientManager.updatePatientDNI(patientId, newDni);

        // Enviar respuesta al doctor
        SendDataViaNetwork sendData = new SendDataViaNetwork(socket);
        if (success) {
            sendData.sendStrings("Patient  DNI updated successfully.");
        } else {
            sendData.sendStrings("Error updating patient name.");
        }
    }

    public static void updatePatientDob(Socket socket) throws IOException {
        // Recibir el ID del paciente
        ReceiveDataViaNetwork receiveData = new ReceiveDataViaNetwork(socket);
        int patientId = receiveData.receiveInt();

        // Recibir el nuevo nombre
        String newDobstr= receiveData.receiveString();
        java.sql.Date newDob = Date.valueOf(newDobstr);

        // Crear una instancia de JDBCPatientManager
        JDBCPatientManager patientManager = new JDBCPatientManager(new ConnectionManager());

        // Llamar al método de JDBCPatientManager para actualizar el nombre
        boolean success = patientManager.updatePatientDob(patientId,newDob);

        // Enviar respuesta al doctor
        SendDataViaNetwork sendData = new SendDataViaNetwork(socket);
        if (success) {
            sendData.sendStrings("Patient name updated successfully.");
        } else {
            sendData.sendStrings("Error updating patient name.");
        }
    }

    public static void updatePatientSex(Socket socket) throws IOException {
        // Recibir el ID del paciente
        ReceiveDataViaNetwork receiveData = new ReceiveDataViaNetwork(socket);
        int patientId = receiveData.receiveInt();

        // Recibir el nuevo nombre
        String newSex = receiveData.receiveString();

        // Crear una instancia de JDBCPatientManager
        JDBCPatientManager patientManager = new JDBCPatientManager(new ConnectionManager());

        // Llamar al método de JDBCPatientManager para actualizar el nombre
        boolean success = patientManager.updatePatientSex(patientId, newSex);

        // Enviar respuesta al doctor
        SendDataViaNetwork sendData = new SendDataViaNetwork(socket);
        if (success) {
            sendData.sendStrings("Patient name updated successfully.");
        } else {
            sendData.sendStrings("Error updating patient name.");
        }
    }
    public static void updatePatientPhone(Socket socket) throws IOException {
        // Recibir el ID del paciente
        ReceiveDataViaNetwork receiveData = new ReceiveDataViaNetwork(socket);
        int patientId = receiveData.receiveInt();

        // Recibir el nuevo nombre
        int newPhone = receiveData.receiveInt();

        // Crear una instancia de JDBCPatientManager
        JDBCPatientManager patientManager = new JDBCPatientManager(new ConnectionManager());

        // Llamar al método de JDBCPatientManager para actualizar el nombre
        boolean success = patientManager.updatePatientPhone(patientId, newPhone);

        // Enviar respuesta al doctor
        SendDataViaNetwork sendData = new SendDataViaNetwork(socket);
        if (success) {
            sendData.sendStrings("Patient name updated successfully.");
        } else {
            sendData.sendStrings("Error updating patient name.");
        }
    }
    public static void updatePatientInsurance(Socket socket) throws IOException {
        // Recibir el ID del paciente
        ReceiveDataViaNetwork receiveData = new ReceiveDataViaNetwork(socket);
        int patientId = receiveData.receiveInt();

        // Recibir el nuevo nombre
        int newInsurance = receiveData.receiveInt();

        // Crear una instancia de JDBCPatientManager
        JDBCPatientManager patientManager = new JDBCPatientManager(new ConnectionManager());

        // Llamar al método de JDBCPatientManager para actualizar el nombre
        boolean success = patientManager.updatePatientInsurance(patientId, newInsurance);

        // Enviar respuesta al doctor
        SendDataViaNetwork sendData = new SendDataViaNetwork(socket);
        if (success) {
            sendData.sendStrings("Patient name updated successfully.");
        } else {
            sendData.sendStrings("Error updating patient name.");
        }
    }
    public static void updatePatientEmail(Socket socket) throws IOException {
        // Recibir el ID del paciente
        ReceiveDataViaNetwork receiveData = new ReceiveDataViaNetwork(socket);
        int patientId = receiveData.receiveInt();

        // Recibir el nuevo nombre
        String newEmail = receiveData.receiveString();

        // Crear una instancia de JDBCPatientManager
        JDBCPatientManager patientManager = new JDBCPatientManager(new ConnectionManager());

        // Llamar al método de JDBCPatientManager para actualizar el nombre
        boolean success = patientManager.updatePatientEmail(patientId, newEmail);

        // Enviar respuesta al doctor
        SendDataViaNetwork sendData = new SendDataViaNetwork(socket);
        if (success) {
            sendData.sendStrings("Patient name updated successfully.");
        } else {
            sendData.sendStrings("Error updating patient name.");
        }
    }

    private static void patientInsertMedicalInformartion(ReceiveDataViaNetwork recieveDataViaNetwork, SendDataViaNetwork sendDataViaNetwork, Socket socket, PatientManager patientManager, SymptomManager symptomManager, MedicalInformationManager medicalInformationManager) throws IOException {
        try{
            String message = recieveDataViaNetwork.receiveString();
            if(message.equals("send symptoms")){
                //sendDataViaNetwork.sendSymptom();
            }
            System.out.println(message);
            if(message.equals("OK")){
                System.out.println("Medical information in process");
                //Symptom symptom = symptomManager.getSymptomById()
                //sendDataViaNetwork.sendSymptom(symptom);
                MedicalInformation medicalInformation = recieveDataViaNetwork.receiveMedicalInformation();
                System.out.println(medicalInformation.toString());
                //medicalInformationManager.insertMedicalInformation(medicalInformation); //añadimos medical info a la DB
            }

        }catch (Exception e) {
            System.out.println("Error or client disconnected");
            // releaseResources(recieveDataViaNetwork,sendDataViaNetwork,socket);
        }

    }
}

