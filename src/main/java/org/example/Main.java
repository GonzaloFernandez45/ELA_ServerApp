package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Date;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import interfaces.*;
import jdbc.*;
import ReceiveData.*;
import pojos.*;
import jdbc.JDBCPatientManager;

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
            JDBCSymptomManager symptomManager = new JDBCSymptomManager(conMan);
            JDBCMedicalInformationManager medicalInformationManager = new JDBCMedicalInformationManager(conMan);
            Socket socket = serverSocket.accept();
            JDBCDoctorManager doctorManager = new JDBCDoctorManager(conMan);

            ReceiveDataViaNetwork recieveDataViaNetwork = null;
            SendDataViaNetwork sendDataViaNetwork = null;

                try {
                    recieveDataViaNetwork = new ReceiveDataViaNetwork(socket);
                    sendDataViaNetwork = new SendDataViaNetwork(socket);
                    System.out.println("Socket accepted");

                    int message = recieveDataViaNetwork.receiveInt();

                    if(message == 1){
                        sendDataViaNetwork.sendStrings("PATIENT");
                        patientMenu(sendDataViaNetwork, recieveDataViaNetwork, socket, patientManager, userManager, symptomManager, medicalInformationManager); // atiende a este cliente y vuelve a escuchar
                    } else if (message == 2) {
                        sendDataViaNetwork.sendStrings("Doctor");
                        doctorMenu(sendDataViaNetwork, recieveDataViaNetwork, socket, doctorManager, userManager, symptomManager, medicalInformationManager); // atiende a este cliente y vuelve a escuchar
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


    private static void patientMenu(SendDataViaNetwork sendDataViaNetwork, ReceiveDataViaNetwork recieveDataViaNetwork, Socket socket, PatientManager patientManager, JDBCUserManager userManager, SymptomManager symptomManager, MedicalInformationManager medicalInformationManagerr) throws IOException {
          try{
            boolean patientMenu = true;

            while(patientMenu){
                int opcion = recieveDataViaNetwork.receiveInt();
                switch(opcion){
                    case 1:
                        logInPatient(recieveDataViaNetwork, sendDataViaNetwork,socket,patientManager,userManager, symptomManager, medicalInformationManagerr);
                        break;
                    case 2:
                        System.out.println("Patient register");
                        patientRegister(recieveDataViaNetwork,sendDataViaNetwork, socket, patientManager, userManager, symptomManager, medicalInformationManagerr);
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

    private static void patientRegister(ReceiveDataViaNetwork recieveDataViaNetwork, SendDataViaNetwork sendDataViaNetwork, Socket socket, PatientManager patientManager, UserManager userManager, SymptomManager symptomManager, MedicalInformationManager medicalInformationManager) throws IOException {
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
                    menuPaciente(patient, sendDataViaNetwork,recieveDataViaNetwork , socket, patientManager, symptomManager, medicalInformationManager);

                }else{
                    System.out.println("Error in register");

                }
            }catch(IOException ex){
                System.out.println("Error or client disconnected");
                releaseResources(recieveDataViaNetwork,sendDataViaNetwork,socket);
            }
    }

    private static void menuPaciente(Patient patient, SendDataViaNetwork sendDataViaNetwork, ReceiveDataViaNetwork recieveDataViaNetwork, Socket socket, PatientManager patientManager, SymptomManager symptomManager, MedicalInformationManager medicalInformationManager) throws IOException {
        try{
            boolean patientMenu = true;

            while(patientMenu){
                int opcion = recieveDataViaNetwork.receiveInt();
                switch(opcion){
                    case 1:
                        System.out.println("Insert medical info");
                        patientInsertMedicalInformartion(patient, recieveDataViaNetwork, sendDataViaNetwork, socket, patientManager, symptomManager, medicalInformationManager);
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

    private static void logInPatient(ReceiveDataViaNetwork recieveDataViaNetwork, SendDataViaNetwork sendDataViaNetwork, Socket socket, PatientManager patientManager, UserManager userManager, SymptomManager symptomManager, MedicalInformationManager medicalInformationManager) throws IOException {
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
                        menuPaciente(patient, sendDataViaNetwork,recieveDataViaNetwork , socket, patientManager, symptomManager, medicalInformationManager);
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

    private static void doctorMenu(SendDataViaNetwork sendDataViaNetwork, ReceiveDataViaNetwork recieveDataViaNetwork, Socket socket, DoctorManager doctorManager, JDBCUserManager userManager, SymptomManager symptomManager, MedicalInformationManager medicalInformationManagerr) throws IOException {
        try{
            boolean doctorMenu = true;

            while(doctorMenu){
                int opcion = recieveDataViaNetwork.receiveInt();
                switch(opcion){
                    case 1:
                        System.out.println("Doctor Log in");
                        logInDoctor(recieveDataViaNetwork, sendDataViaNetwork,socket,doctorManager,userManager, symptomManager, medicalInformationManagerr);
                        break;
                    case 2:
                        System.out.println("Doctor register");
                        doctorRegister(recieveDataViaNetwork,sendDataViaNetwork, socket, doctorManager, userManager, symptomManager, medicalInformationManagerr);
                        break;
                    case 3:
                        doctorMenu = false;
                        System.out.println("Doctor disconnected");
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

    private static void doctorRegister(ReceiveDataViaNetwork recieveDataViaNetwork, SendDataViaNetwork sendDataViaNetwork, Socket socket, DoctorManager doctorManager, UserManager userManager, SymptomManager symptomManager, MedicalInformationManager medicalInformationManager) throws IOException {
        try{
            String message = recieveDataViaNetwork.receiveString();
            System.out.println(message);

            if(message.equals("OK")){
                System.out.println("Registering Doctor...");
                Doctor doctor = recieveDataViaNetwork.receiveDoctor();
                System.out.println(doctor.toString());
                User user = recieveDataViaNetwork.recieveUser();
                System.out.println(user.toString());
                doctorManager.addDoctor(doctor); //Primero añadir al paciente a la DB

                int doctor_id = doctorManager.getDoctorIDFromEmail(doctor.getEmail()); //Obtener su ID

                user.setDoctor_id(doctor_id); //Asignar la Foreign Key al usuario
                userManager.addUser(user); //Añadir el usuario a la DB

                sendDataViaNetwork.sendStrings("SUCCESS");

                doctor.setId(doctor_id);
                menuDoctor(doctor, sendDataViaNetwork,recieveDataViaNetwork , socket, doctorManager, symptomManager, medicalInformationManager);

            }else{
                System.out.println("Error in register");

            }
        }catch(IOException ex){
            System.out.println("Error or client disconnected");
            releaseResources(recieveDataViaNetwork,sendDataViaNetwork,socket);
        }
    }

    private static void menuDoctor(Doctor doctor, SendDataViaNetwork sendDataViaNetwork, ReceiveDataViaNetwork recieveDataViaNetwork, Socket socket, DoctorManager doctorManager, SymptomManager symptomManager, MedicalInformationManager medicalInformationManager) throws IOException {
        try{
            boolean doctorMenu = true;

            while(doctorMenu){
                int opcion = recieveDataViaNetwork.receiveInt();
                switch(opcion){
                    case 1:
                        System.out.println("Insert medical info");
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
    private static void logInDoctor(ReceiveDataViaNetwork recieveDataViaNetwork, SendDataViaNetwork sendDataViaNetwork, Socket socket, DoctorManager doctorManager, UserManager userManager, SymptomManager symptomManager, MedicalInformationManager medicalInformationManager) throws IOException {
        try{
            sendDataViaNetwork.sendStrings("Doctor log in");
            String message = recieveDataViaNetwork.receiveString();
            System.out.println(message);
            Role role = new Role("Doctor");

            if(message.equals("OK")){
                User user = recieveDataViaNetwork.recieveUser();
                boolean correctPassword = userManager.checkPassword(new String(user.getPassword()), user.getEmail());
                if(correctPassword){
                    sendDataViaNetwork.sendStrings("SUCCESS");

                    int doctor_id = doctorManager.getDoctorIDFromEmail(user.getEmail());
                    Doctor doctor = doctorManager.getDoctorbyId(doctor_id);
                    user.setDoctor_id(doctor_id);

                    System.out.println(doctor.toString());

                    sendDataViaNetwork.sendDoctor(doctor);
                    //
                    menuDoctor(doctor, sendDataViaNetwork,recieveDataViaNetwork , socket, doctorManager, symptomManager, medicalInformationManager);
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


    public static void addFeedback (Socket socket, ReceiveDataViaNetwork receiveDataViaNetwork,SendDataViaNetwork sendDataViaNetwork ) throws IOException {
          // Recibir el ID del paciente desde el cliente

        // Recibir el feedback del doctor
        int patientid = receiveDataViaNetwork.receiveInt();
        String feedback = receiveDataViaNetwork.receiveString();  // Recibir el feedback
        ConnectionManager conMan = new ConnectionManager();
        JDBCPatientManager patientManager= new JDBCPatientManager(conMan);

        // Procesar el feedback (en este caso, guardarlo en la base de datos)
        String responseMessage = patientManager.addFeedback(patientid,feedback);// Procesar y guardar el feedback

        // Enviar respuesta al doctor
        sendDataViaNetwork.sendStrings(responseMessage);

    }
    public static void updatePatientName(Socket socket, ReceiveDataViaNetwork receiveDataViaNetwork, SendDataViaNetwork sendDataViaNetwork) throws IOException {

        String newName = receiveDataViaNetwork.receiveString();
        int patient_id = receiveDataViaNetwork.receiveInt();
        if( newName.isEmpty() || newName== null){
            System.out.println("No new name ");
            return;
        }

        // Crear una instancia de JDBCPatientManager
        ConnectionManager conMan= new ConnectionManager();
        JDBCPatientManager patientManager = new JDBCPatientManager(conMan);

        // Llamar al método de JDBCPatientManager para actualizar el nombre
        boolean success = patientManager.updatePatientName(patient_id, newName);

        if (success) {
            sendDataViaNetwork.sendStrings("Patient name updated successfully.");
        } else {
            sendDataViaNetwork.sendStrings("Error updating patient name.");
        }
    }

    public static void updatePatientSurname(Socket socket,ReceiveDataViaNetwork receiveDataViaNetwork, SendDataViaNetwork sendDataViaNetwork) throws IOException {
        String newSurname = receiveDataViaNetwork.receiveString();
        int patient_id = receiveDataViaNetwork.receiveInt();
        if( newSurname.isEmpty() || newSurname== null){
            System.out.println("No new surname ");
            return;
        }

        // Crear una instancia de JDBCPatientManager
        ConnectionManager conMan= new ConnectionManager();
        JDBCPatientManager patientManager = new JDBCPatientManager(conMan);

        // Llamar al método de JDBCPatientManager para actualizar el nombre
        boolean success = patientManager.updatePatientSurname(patient_id, newSurname);

        // Enviar respuesta al doctor
        SendDataViaNetwork sendData = new SendDataViaNetwork(socket);
        if (success) {
            sendData.sendStrings("Patient surname updated successfully.");
        } else {
            sendData.sendStrings("Error updating patient surname.");
        }
    }
    public static void updatePatientDNI(Socket socket,ReceiveDataViaNetwork receiveDataViaNetwork,SendDataViaNetwork sendDataViaNetwork) throws IOException {
        // Recibir el ID del paciente
        String newDNI = receiveDataViaNetwork.receiveString();
        int patient_id = receiveDataViaNetwork.receiveInt();
        if (newDNI.isEmpty() || newDNI== null){
            System.out.println("No new DNI");
        }


        // Crear una instancia de JDBCPatientManager
        JDBCPatientManager patientManager = new JDBCPatientManager(new ConnectionManager());

        // Llamar al método de JDBCPatientManager para actualizar el nombre
        boolean success = patientManager.updatePatientDNI(patient_id, newDNI);
        if (success) {
            sendDataViaNetwork.sendStrings("Patient  DNI updated successfully.");
        } else {
            sendDataViaNetwork.sendStrings("Error updating patient DNI.");
        }
    }

    public static void updatePatientDob(Socket socket, ReceiveDataViaNetwork receiveDataViaNetwork,SendDataViaNetwork sendDataViaNetwork) throws IOException {

        // Recibir el nuevo nombre
        int patient_id= receiveDataViaNetwork.receiveInt();
        String newDobstr= receiveDataViaNetwork.receiveString();
        java.sql.Date newDob = Date.valueOf(newDobstr);

        // Crear una instancia de JDBCPatientManager
        JDBCPatientManager patientManager = new JDBCPatientManager(new ConnectionManager());

        // Llamar al método de JDBCPatientManager para actualizar el nombre
        boolean success = patientManager.updatePatientDob(patient_id,newDob);

        // Enviar respuesta al doctor
        SendDataViaNetwork sendData = new SendDataViaNetwork(socket);
        if (success) {
            sendData.sendStrings("Patient dob updated successfully.");
        } else {
            sendData.sendStrings("Error updating patient dob.");
        }
    }

    public static void updatePatientSex(Socket socket, ReceiveDataViaNetwork receiveDataViaNetwork, SendDataViaNetwork sendDataViaNetwork) throws IOException {

        // Recibir el nuevo nombre
        String newSex = receiveDataViaNetwork.receiveString();
        int patient_id = receiveDataViaNetwork.receiveInt();

        // Crear una instancia de JDBCPatientManager
        JDBCPatientManager patientManager = new JDBCPatientManager(new ConnectionManager());

        // Llamar al método de JDBCPatientManager para actualizar el nombre
        boolean success = patientManager.updatePatientSex(patient_id, newSex);

        // Enviar respuesta al doctor
        SendDataViaNetwork sendData = new SendDataViaNetwork(socket);
        if (success) {
            sendData.sendStrings("Patient name updated successfully.");
        } else {
            sendData.sendStrings("Error updating patient name.");
        }
    }
    public static void updatePatientPhone(Socket socket, ReceiveDataViaNetwork receiveDataViaNetwork,SendDataViaNetwork sendDataViaNetwork) throws IOException {
        // Recibir el ID del paciente
        int patientId = receiveDataViaNetwork.receiveInt();

        // Recibir el nuevo nombre
        int newPhone = receiveDataViaNetwork.receiveInt();
        if (newPhone == 0){
            System.out.println("No new phone ");
            return;
        }

        // Crear una instancia de JDBCPatientManager
        JDBCPatientManager patientManager = new JDBCPatientManager(new ConnectionManager());

        // Llamar al método de JDBCPatientManager para actualizar el nombre
        boolean success = patientManager.updatePatientPhone(patientId, newPhone);

        if (success) {
            sendDataViaNetwork.sendStrings("Patient phone updated successfully.");
        } else {
            sendDataViaNetwork.sendStrings("Error updating patient phone.");
        }
    }
    public static void updatePatientInsurance(Socket socket, ReceiveDataViaNetwork receiveDataViaNetwork,SendDataViaNetwork sendDataViaNetwork) throws IOException {

        int patientId = receiveDataViaNetwork.receiveInt();

        // Recibir el nuevo nombre
        int newInsurance = receiveDataViaNetwork.receiveInt();
        if (newInsurance == 0){
            System.out.println("No new insurance");
            return;
        }

        // Crear una instancia de JDBCPatientManager
        JDBCPatientManager patientManager = new JDBCPatientManager(new ConnectionManager());

        // Llamar al método de JDBCPatientManager para actualizar el nombre
        boolean success = patientManager.updatePatientInsurance(patientId, newInsurance);

        if (success) {
            sendDataViaNetwork.sendStrings("Patient insurance updated successfully.");
        } else {
            sendDataViaNetwork.sendStrings("Error updating patient insurance.");
        }
    }
    public static void updatePatientEmail(Socket socket, ReceiveDataViaNetwork receiveDataViaNetwork,SendDataViaNetwork sendDataViaNetwork) throws IOException {
        int patientId = receiveDataViaNetwork.receiveInt();

        // Recibir el nuevo nombre
        String newEmail = receiveDataViaNetwork.receiveString();
        if (newEmail.isEmpty() || newEmail== null){
            System.out.println("No new email");
            return;
        }

        // Crear una instancia de JDBCPatientManager
        JDBCPatientManager patientManager = new JDBCPatientManager(new ConnectionManager());

        // Llamar al método de JDBCPatientManager para actualizar el nombre
        boolean success = patientManager.updatePatientEmail(patientId, newEmail);
        if (success) {
            sendDataViaNetwork.sendStrings("Patient name updated successfully.");
        } else {
            sendDataViaNetwork.sendStrings("Error updating patient name.");
        }
    }

    private static void patientInsertMedicalInformartion(Patient patient, ReceiveDataViaNetwork recieveDataViaNetwork, SendDataViaNetwork sendDataViaNetwork, Socket socket, PatientManager patientManager, SymptomManager symptomManager, MedicalInformationManager medicalInformationManager) throws IOException {
        try{
            String message = recieveDataViaNetwork.receiveString();
            if(message.equals("SEND SYMPTOMS")){
                sendDataViaNetwork.sendStrings("OK");

                //lega hasta aqui
                List<Symptom> listSymptoms = symptomManager.listSymptoms();
                sendDataViaNetwork.sendSymptoms(listSymptoms);

                System.out.println("Medical information in process");
                MedicalInformation medicalInformation = recieveDataViaNetwork.receiveMedicalInformation();
                System.out.println(medicalInformation.toString());
                if(medicalInformation != null){
                    sendDataViaNetwork.sendStrings("RECEIVED MEDICAL INFORMATION");
                    medicalInformationManager.insertMedicalInformation(medicalInformation); //añadimos medical info a la DB
                    menuPaciente(patient, sendDataViaNetwork,recieveDataViaNetwork , socket, patientManager, symptomManager, medicalInformationManager);
                }else{
                    sendDataViaNetwork.sendStrings("ERROR");
                }

            }

        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error or client disconnected");
            // releaseResources(recieveDataViaNetwork,sendDataViaNetwork,socket);
        }

    }
}

