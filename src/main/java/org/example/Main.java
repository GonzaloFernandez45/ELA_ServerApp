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
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.List;

public class Main {

    private static int activeClients = 0;
    /**
     * The server's running state
     */
    private static boolean running = true;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8888);
        ConnectionManager conMan = new ConnectionManager();
        JDBCPatientManager patientManager = new JDBCPatientManager(conMan);
        JDBCUserManager userManager = new JDBCUserManager(conMan);
        JDBCSymptomManager symptomManager = new JDBCSymptomManager(conMan);
        JDBCMedicalInformationManager medicalInformationManager = new JDBCMedicalInformationManager(conMan);
        Socket socket = serverSocket.accept();
        JDBCDoctorManager doctorManager = new JDBCDoctorManager(conMan);
        JDBCAdministratorManager administratorManager = new JDBCAdministratorManager(conMan);

        ReceiveDataViaNetwork recieveDataViaNetwork = null;
        SendDataViaNetwork sendDataViaNetwork = null;

        try {
            recieveDataViaNetwork = new ReceiveDataViaNetwork(socket);
            sendDataViaNetwork = new SendDataViaNetwork(socket);
            System.out.println("Socket accepted");

            int message = recieveDataViaNetwork.receiveInt();

            if (message == 1) {
                sendDataViaNetwork.sendStrings("PATIENT");
                patientMenu(sendDataViaNetwork, recieveDataViaNetwork, socket, patientManager, userManager, symptomManager, medicalInformationManager); // atiende a este cliente y vuelve a escuchar
            } else if (message == 2) {
                sendDataViaNetwork.sendStrings("DOCTOR");
                doctorMenu(sendDataViaNetwork, recieveDataViaNetwork, socket, doctorManager, userManager, symptomManager, medicalInformationManager, patientManager); // atiende a este cliente y vuelve a escuchar
            } else if (message == 3) {
                sendDataViaNetwork.sendStrings("ADMIN");
                AdminMenu(sendDataViaNetwork,recieveDataViaNetwork,socket,administratorManager,userManager);
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
        try {
            boolean patientMenu = true;

            while (patientMenu) {
                int opcion = recieveDataViaNetwork.receiveInt();
                switch (opcion) {
                    case 1:
                        logInPatient(recieveDataViaNetwork, sendDataViaNetwork, socket, patientManager, userManager, symptomManager, medicalInformationManagerr);
                        break;
                    case 2:
                        System.out.println("Patient register");
                        patientRegister(recieveDataViaNetwork, sendDataViaNetwork, socket, patientManager, userManager, symptomManager, medicalInformationManagerr);
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
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            releaseResources(recieveDataViaNetwork, sendDataViaNetwork, socket);
        }
    }

    private static void patientRegister(ReceiveDataViaNetwork recieveDataViaNetwork, SendDataViaNetwork sendDataViaNetwork, Socket socket, PatientManager patientManager, UserManager userManager, SymptomManager symptomManager, MedicalInformationManager medicalInformationManager) throws IOException {
        try {
            String message = recieveDataViaNetwork.receiveString();
            System.out.println(message);

            if (message.equals("OK")) {
                System.out.println("Registering patient...");
                Patient patient = recieveDataViaNetwork.recievePatient();
                System.out.println(patient.toString());
                User user = recieveDataViaNetwork.recieveUser();
                System.out.println(user.toString());
                patientManager.addPatient(patient); //Primero añadir al paciente a la DB

                int patient_id = patientManager.getPatientIDFromEmail(patient.getEmail()); //Obtener su ID
                System.out.println("Patient ID from email = " + patient_id);
                user.setPatient_id(patient_id); //Asignar la Foreign Key al usuario
                userManager.addUser(user); //Añadir el usuario a la DB

                sendDataViaNetwork.sendStrings("SUCCESS");

                patient.setId(patient_id);
                menuPaciente(patient, sendDataViaNetwork, recieveDataViaNetwork, socket, patientManager, symptomManager, medicalInformationManager);

            } else {
                System.out.println("Error in register");

            }
        } catch (IOException ex) {
            System.out.println("Error or client disconnected");
            releaseResources(recieveDataViaNetwork, sendDataViaNetwork, socket);
        }
    }

    private static void menuPaciente(Patient patient, SendDataViaNetwork sendDataViaNetwork, ReceiveDataViaNetwork recieveDataViaNetwork, Socket socket, PatientManager patientManager, SymptomManager symptomManager, MedicalInformationManager medicalInformationManager) throws IOException {
        try {
            boolean patientMenu = true;

            while (patientMenu) {
                int opcion = recieveDataViaNetwork.receiveInt();

                switch (opcion) {
                    case 1:
                        System.out.println("Option 1: Insert medical info");
                        patientInsertMedicalInformartion(patient, recieveDataViaNetwork, sendDataViaNetwork, socket, patientManager, symptomManager, medicalInformationManager);
                        break;

                    case 2:
                        System.out.println("Option 2: Receiving Signal...");
                        patientReceiveAndSaveSignal(patient, recieveDataViaNetwork, sendDataViaNetwork);
                        break;

                    case 3:
                        System.out.println("Option 3: Sending doctor feedback");
                        patientSeeDoctorFeedback(patient, recieveDataViaNetwork, sendDataViaNetwork, socket, patientManager, symptomManager, medicalInformationManager);
                        break;

                    case 0:
                        System.out.println("Client disconnected (Option 0)");
                        patientMenu = false;
                        break;

                    default:
                        System.out.println("Invalid option received: " + opcion);
                        break;
                }
            }
        } catch (Exception ex) {
            System.out.println("Error in patient menu: " + ex.getMessage());
            releaseResources(recieveDataViaNetwork, sendDataViaNetwork, socket);
        }
    }


    private static void logInPatient(ReceiveDataViaNetwork recieveDataViaNetwork, SendDataViaNetwork sendDataViaNetwork, Socket socket, PatientManager patientManager, UserManager userManager, SymptomManager symptomManager, MedicalInformationManager medicalInformationManager) throws IOException {
            try{
                sendDataViaNetwork.sendStrings("Patient log in");
                String message = recieveDataViaNetwork.receiveString();
                System.out.println(message);
                Role role = new Role("Patient");

            if (message.equals("OK")) {
                User user = recieveDataViaNetwork.recieveUser();
                boolean correctPassword = userManager.checkPassword(new String(user.getPassword()), user.getEmail());
                if (correctPassword) {
                    sendDataViaNetwork.sendStrings("SUCCESS");

                    int patient_id = patientManager.getPatientIDFromEmail(user.getEmail());
                    Patient patient = patientManager.getPatientbyId(patient_id);
                    user.setPatient_id(patient_id);

                    System.out.println(patient.toString());

                    sendDataViaNetwork.sendPatient(patient);
                    //
                    menuPaciente(patient, sendDataViaNetwork, recieveDataViaNetwork, socket, patientManager, symptomManager, medicalInformationManager);
                } else {
                    sendDataViaNetwork.sendStrings("ERROR");
                }
            } else {
                System.out.println("Error in login");
            }


        } catch (IOException e) {
            System.out.println("Error or client disconnected");
            releaseResources(recieveDataViaNetwork, sendDataViaNetwork, socket);
        }
    }

    private static void releaseResources(ReceiveDataViaNetwork recieveDataViaNetwork, SendDataViaNetwork sendDataViaNetwork, Socket socket) {
        if (sendDataViaNetwork != null && recieveDataViaNetwork != null) {
            sendDataViaNetwork.releaseResources();
            recieveDataViaNetwork.releaseResources();
        }
        try {
            socket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void doctorMenu(SendDataViaNetwork sendDataViaNetwork, ReceiveDataViaNetwork recieveDataViaNetwork, Socket socket, DoctorManager doctorManager, JDBCUserManager userManager, SymptomManager symptomManager, MedicalInformationManager medicalInformationManagerr, PatientManager patientManager) throws IOException {
        try {
            boolean doctorMenu = true;

            while (doctorMenu) {
                int opcion = recieveDataViaNetwork.receiveInt();
                switch (opcion) {
                    case 1:
                        System.out.println("Doctor Log in");
                        logInDoctor(recieveDataViaNetwork, sendDataViaNetwork, socket, doctorManager, userManager, symptomManager, medicalInformationManagerr, patientManager);
                        break;
                    case 2:
                        System.out.println("Doctor register");
                        doctorRegister(recieveDataViaNetwork, sendDataViaNetwork, socket, doctorManager, userManager, symptomManager, medicalInformationManagerr, patientManager);
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
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            releaseResources(recieveDataViaNetwork, sendDataViaNetwork, socket);
        }
    }

    private static void doctorRegister(ReceiveDataViaNetwork recieveDataViaNetwork, SendDataViaNetwork sendDataViaNetwork, Socket socket, DoctorManager doctorManager, UserManager userManager, SymptomManager symptomManager, MedicalInformationManager medicalInformationManager, PatientManager patientManager) throws IOException {
        try {
            String message = recieveDataViaNetwork.receiveString();
            System.out.println(message);

            if (message.equals("OK")) {
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

                System.out.println("Doctor requested patient list");;
                Patient patient = selectPatientForDoctor(doctor, sendDataViaNetwork, recieveDataViaNetwork, patientManager);

                menuDoctor(patient, doctor, sendDataViaNetwork, recieveDataViaNetwork, socket, doctorManager,symptomManager, medicalInformationManager, patientManager);

            } else {
                System.out.println("Error in register");

            }
        } catch (IOException ex) {
            System.out.println("Error or client disconnected");
            releaseResources(recieveDataViaNetwork, sendDataViaNetwork, socket);
        }
    }
    private static Patient selectPatientForDoctor(Doctor doctor, SendDataViaNetwork sendData, ReceiveDataViaNetwork receiveData, PatientManager patientManager
    ) throws IOException {

        // 1. Obtener lista de pacientes
        List<Patient> patients = patientManager.listPatients();

        StringBuilder sb = new StringBuilder();
        sb.append("PATIENT LIST:\n");
        for (Patient p : patients) {
            sb.append("ID: ").append(p.getId())
                    .append(" | Name: ").append(p.getName()).append(" ").append(p.getSurname())
                    .append("\n");
        }

        // 2. Enviar lista al doctor
        sendData.sendStrings(sb.toString());

        // 3. Recibir el ID seleccionado por el doctor
        int selectedId = receiveData.receiveInt();

        System.out.println("Selecting patient...");
        // 4. Buscar paciente
        Patient selected = patientManager.getPatientbyId(selectedId);
        System.out.println(selected);

        if (selected != null) {
            System.out.println("Doctor selected patient: " + selected.getName());
            return selected;
        } else {
            sendData.sendStrings("ERROR: Patient not found");
        }
        return selected;
    }

    private static void logInDoctor(ReceiveDataViaNetwork recieveDataViaNetwork, SendDataViaNetwork sendDataViaNetwork, Socket socket, DoctorManager doctorManager, UserManager userManager, SymptomManager symptomManager, MedicalInformationManager medicalInformationManager, PatientManager patientManager) throws IOException {
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

                    System.out.println("Doctor requested patient list");;
                    Patient patient = selectPatientForDoctor(doctor, sendDataViaNetwork, recieveDataViaNetwork, patientManager);


                    menuDoctor(patient, doctor, sendDataViaNetwork, recieveDataViaNetwork, socket, doctorManager,symptomManager, medicalInformationManager, patientManager);

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

    private static void menuDoctor(Patient patient, Doctor doctor, SendDataViaNetwork sendDataViaNetwork, ReceiveDataViaNetwork recieveDataViaNetwork, Socket socket, DoctorManager doctorManager, SymptomManager symptomManager, MedicalInformationManager medicalInformationManager, PatientManager patientManager) throws IOException {
        try{

            boolean doctorMenu = true;
            while (doctorMenu) {
                int opcion = recieveDataViaNetwork.receiveInt();
                switch (opcion) {
                    case 1:
                        System.out.println("SELECTED: View patient details");
                        viewPatient(patient, socket,recieveDataViaNetwork,sendDataViaNetwork);
                        doctorViewMedicalInformation(patient,doctor,socket,recieveDataViaNetwork,sendDataViaNetwork,doctorManager,patientManager,medicalInformationManager, symptomManager);
                        break;
                    case 2:
                        System.out.println("SELECTED: Add feedback");
                        //addFeedback(socket, recieveDataViaNetwork,sendDataViaNetwork);
                        selectAndUpdateFeedback(socket, recieveDataViaNetwork, sendDataViaNetwork, medicalInformationManager);
                        break;
                    case 3:
                        System.out.println("SELECTED: View recorded signal");

                        break;
                    case 4:
                        System.out.println("SELECTED: Change patient data");
                        updatePatientData(socket, recieveDataViaNetwork, sendDataViaNetwork);
                        break;
                    case 0:
                        System.out.println("0. Exit");
                        break;
                    default:
                        System.out.println("Invalid option");
                        break;
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            releaseResources(recieveDataViaNetwork, sendDataViaNetwork, socket);
        }
    }


    public static void viewPatient(Patient patient, Socket socket, ReceiveDataViaNetwork receiveDataViaNetwork, SendDataViaNetwork sendDataViaNetwork) throws IOException {
        System.out.println("The doctor has requested the information of the patient:" + patient.getId());
        System.out.println(patient);
        if(patient != null){
            sendDataViaNetwork.sendPatient(patient);
        }
        else {
            sendDataViaNetwork.sendStrings("ERROR - PATIENT NOT FOUND");
        }

    }

    //    public static void viewPatientMedInfo(Socket socket, ReceiveDataViaNetwork receiveDataViaNetwork,SendDataViaNetwork sendDataViaNetwork)throws IOException{
//        int patient_id = receiveDataViaNetwork.receiveInt();
//        System.out.println("The doctor has requested the Medical information of the patient:");
//        ConnectionManager connectionManager = new ConnectionManager();
//        JDBCMedicalInformationManager medicalInformationManager = new JDBCMedicalInformationManager(connectionManager);
//        List<MedicalInformation> medicalInformation = medicalInformationManager.getMedicalInfoByPatientId(patient_id);
//        sendDataViaNetwork.sendInt(medicalInformation.size());
//        for(MedicalInformation mi : medicalInformation){
//            sendDataViaNetwork.sendInt(mi.getId());
//            sendDataViaNetwork.sendStrings(mi.getReportDate().toString());
//            sendDataViaNetwork.sendInt(mi.getMedication().size());
//            for(String medication : mi.getMedication()){
//                sendDataViaNetwork.sendStrings(medication);
//            }
//            sendDataViaNetwork.sendInt(mi.getSymptoms().size());
//            for(Symptom symptom : mi.getSymptoms()){
//                sendDataViaNetwork.sendStrings(symptom.getDescription());
//            }
//        }
//    }
    public static void viewPatientMedInfo(Socket socket, ReceiveDataViaNetwork receiveDataViaNetwork, SendDataViaNetwork sendDataViaNetwork) throws IOException {
        int patient_id = receiveDataViaNetwork.receiveInt();
        System.out.println("The doctor has requested the Medical information of the patient:");
        ConnectionManager connectionManager = new ConnectionManager();
        JDBCMedicalInformationManager medicalInformationManager = new JDBCMedicalInformationManager(connectionManager);
        List<MedicalInformation> medicalInformationList = medicalInformationManager.getMedicalInfoByPatientId(patient_id);
        sendDataViaNetwork.sendMedicalInformationList(medicalInformationList);
    }

    public static void selectAndUpdateFeedback(Socket socket,
                                               ReceiveDataViaNetwork receiveDataViaNetwork,
                                               SendDataViaNetwork sendDataViaNetwork,
                                               MedicalInformationManager medicalInformationManager) throws IOException {

        // Paso 1: Recibir el patientId del médico
        int patientId = receiveDataViaNetwork.receiveInt();

        // Paso 2: Obtener todos los registros de medical_information para el paciente
        List<MedicalInformation> medicalInfoList = medicalInformationManager.getMedicalInfoByPatientId(patientId);

        // Paso 3: Comprobar si hay registros médicos
        if (medicalInfoList.isEmpty()) {
            // Enviar un mensaje al doctor indicando que no hay registros médicos
            sendDataViaNetwork.sendStrings("No medical records found for this patient.");
            return;  // Terminar la ejecución si no hay registros médicos
        }

        // Paso 4: Enviar la lista de registros médicos al doctor
        StringBuilder records = new StringBuilder("Select a record to update feedback:\n");
        for (int i = 0; i < medicalInfoList.size(); i++) {
            MedicalInformation m = medicalInfoList.get(i);
            records.append(i + 1).append(". Date: ").append(m.getReportDate()).append(" Feedback: ").append(m.getFeedback()).append("\n");
        }
        sendDataViaNetwork.sendStrings(records.toString());  // Enviar los registros al doctor

        // Paso 5: Recibir la selección del médico (el índice del registro)
        int selectedIndex = receiveDataViaNetwork.receiveInt();  // El médico selecciona el registro por índice

        // Paso 6: Validar la selección del registro
        if (selectedIndex < 1 || selectedIndex > medicalInfoList.size()) {
            sendDataViaNetwork.sendStrings("Invalid selection. Please try again.");
            return;
        }

        // Paso 7: Obtener el registro seleccionado
        MedicalInformation selectedRecord = medicalInfoList.get(selectedIndex - 1);  // Ajustamos el índice para 0-based

        // Paso 8: Recibir el nuevo feedback del médico
        String newFeedback = receiveDataViaNetwork.receiveString();

        // Paso 9: Actualizar el feedback del registro seleccionado
        boolean success = medicalInformationManager.updateFeedback(selectedRecord.getId(), newFeedback);

        // Paso 10: Preparar el mensaje de respuesta
        String responseMessage;
        if (success) {
            responseMessage = "Feedback updated successfully for the selected record.";
        } else {
            responseMessage = "Error updating feedback for the selected record.";
        }

        // Paso 11: Enviar la respuesta al médico
        sendDataViaNetwork.sendStrings(responseMessage);
    }


    public static void addFeedback(Socket socket, ReceiveDataViaNetwork receiveDataViaNetwork, SendDataViaNetwork sendDataViaNetwork) throws IOException {
        int patient_id = receiveDataViaNetwork.receiveInt();
        ConnectionManager conMan = new ConnectionManager();
        JDBCPatientManager patientManager = new JDBCPatientManager(conMan);
        JDBCMedicalInformationManager medicalInformationManager = new JDBCMedicalInformationManager(conMan);
        List<MedicalInformation> medicalInformation = medicalInformationManager.getMedicalInfoByPatientId(patient_id);
        // Enviamos cada dato relevante de med info
        sendDataViaNetwork.sendInt(medicalInformation.size());
        for (MedicalInformation mi : medicalInformation) {
            sendDataViaNetwork.sendInt(mi.getId());
            sendDataViaNetwork.sendStrings(mi.getReportDate().toString());
        }
        int mi_Id = receiveDataViaNetwork.receiveInt();
        String feedback = receiveDataViaNetwork.receiveString();

        medicalInformationManager.updateMedicalInformation(patient_id, feedback);// Procesar y guardar el feedback


    }


    public static void updatePatientData(Socket socket, ReceiveDataViaNetwork in, SendDataViaNetwork out) throws IOException {
        int patientId = in.receiveInt();

        String newName = in.receiveString();
        String newSurname = in.receiveString();
        int newPhone = in.receiveInt();
        String newEmail = in.receiveString();
        String newdni = in.receiveString();
        String newSex = in.receiveString();
        int newInsurance = in.receiveInt();

        JDBCPatientManager pm = new JDBCPatientManager(new ConnectionManager());
        boolean updated = false;

        if (newName != null && !newName.isEmpty()) {
            pm.updatePatientName(patientId, newName);
            updated = true;
        }

        if (newSurname != null && !newSurname.isEmpty()) {
            pm.updatePatientSurname(patientId, newSurname);
            updated = true;
        }

        if (newPhone != -1) {
            pm.updatePatientPhone(patientId, newPhone);
            updated = true;
        }

        if (newEmail != null && !newEmail.isEmpty()) {
            pm.updatePatientEmail(patientId, newEmail);
            updated = true;
        }
        if (newdni != null && !newdni.isEmpty()) {
            pm.updatePatientDNI(patientId, newdni);
            updated = true;
        }
        if (newSex != null && !newSex.isEmpty()) {
            pm.updatePatientSex(patientId, newSex);
            updated = true;
        }

        if (newInsurance != -1) {
            pm.updatePatientInsurance(patientId, newInsurance);
            updated = true;
        }

        if (updated) {
            out.sendStrings("Patient data updated successfully.");
        } else {
            out.sendStrings("No data updated.");
        }
    }


    private static void patientInsertMedicalInformartion(Patient patient, ReceiveDataViaNetwork recieveDataViaNetwork, SendDataViaNetwork sendDataViaNetwork, Socket socket, PatientManager patientManager, SymptomManager symptomManager, MedicalInformationManager medicalInformationManager) throws IOException {
        try {
            String message = recieveDataViaNetwork.receiveString();
            if (message.equals("SEND SYMPTOMS")) {
                sendDataViaNetwork.sendStrings("OK");

                //lega hasta aqui
                List<Symptom> listSymptoms = symptomManager.listSymptoms();
                System.out.println("Sending symptoms: " + listSymptoms);
                System.out.println("Sending symptoms to client, number of symptoms: " + listSymptoms.size());
                for (Symptom symptom : listSymptoms) {
                    System.out.println("Symptom ID: " + symptom.getId() + ", Description: " + symptom.getDescription());
                }
                sendDataViaNetwork.sendSymptoms(listSymptoms);

                System.out.println("Medical information in process");

                MedicalInformation medicalInformation = recieveDataViaNetwork.receiveMedicalInformation();
                medicalInformation.setPatient_id(patient.getId());
                System.out.println(medicalInformation);
                if (medicalInformation != null) {
                    sendDataViaNetwork.sendStrings("RECEIVED MEDICAL INFORMATION");
                    medicalInformationManager.insertMedicalInformation(medicalInformation); //añadimos medical info a la DB

                    //insertar en symptom_medicalinfromation
                    int medicalInfoId = medicalInformationManager.getMedicalInformationByDate(medicalInformation.getReportDate(), medicalInformation.getPatient_id()).getId();
                    List<Symptom> symptoms = medicalInformation.getSymptoms();

                    for (Symptom symptom : symptoms) {
                        medicalInformationManager.insertSymptomMedicalInformation(medicalInfoId, symptom);
                    }

                    menuPaciente(patient, sendDataViaNetwork, recieveDataViaNetwork, socket, patientManager, symptomManager, medicalInformationManager);
                } else {
                    sendDataViaNetwork.sendStrings("ERROR");
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error or client disconnected");
            releaseResources(recieveDataViaNetwork, sendDataViaNetwork, socket);
        }



    }

    private static void doctorViewMedicalInformation(Patient patient,Doctor doctor, Socket socket, ReceiveDataViaNetwork receiveDataViaNetwork, SendDataViaNetwork sendDataViaNetwork, DoctorManager doctorManager, PatientManager patientManager, MedicalInformationManager medicalInformationManager, SymptomManager symptomManager) throws IOException {
        try {

            // Retrieve all medical information for the patient
            List<MedicalInformation> medicalInfos = medicalInformationManager.getMedicalInfoByPatientId(patient.getId());

            if (medicalInfos == null || medicalInfos.isEmpty()) {
                sendDataViaNetwork.sendStrings("ERROR: No medical information found");
                return;
            }

            //send List to Doctor


            System.out.println("Sending medical information to doctor...");
            sendDataViaNetwork.sendInt(medicalInfos.size());
            // Send each medical information object to the doctor
            for (MedicalInformation info : medicalInfos) {
                List<Symptom> symptoms = symptomManager.getSymptomsOfMedicalInformation(info.getId());
                System.out.println(symptoms);
                info.setSymptoms(symptoms);
                System.out.println(medicalInfos);
                sendDataViaNetwork.sendMedicalInformation(info);
            }

            String response = receiveDataViaNetwork.receiveString();

            if(response.equals("RECEIVED MEDICAL INFORMATION")){
                menuDoctor(patient, doctor, sendDataViaNetwork, receiveDataViaNetwork, socket, doctorManager,symptomManager, medicalInformationManager, patientManager);
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendDataViaNetwork.sendStrings("ERROR: Could not fetch medical information");
        }
    }

    private static void patientSeeDoctorFeedback(Patient patient, ReceiveDataViaNetwork recieveDataViaNetwork, SendDataViaNetwork sendDataViaNetwork, Socket socket, PatientManager patientManager, SymptomManager symptomManager, MedicalInformationManager medicalInformationManager) {

        try {
            String message = recieveDataViaNetwork.receiveString();

            if (message.equals("REQUEST FEEDBACK")) {
                System.out.println("Received request feedback, sending feedback");
                sendDataViaNetwork.sendStrings("OK");

                //buscar por fecha el medical report
                String dateString = recieveDataViaNetwork.receiveString();
                Date date = Date.valueOf(dateString);

                MedicalInformation medicalInformation = medicalInformationManager.getMedicalInformationByDate(date, patient.getId());
                //need to set symptoms and medication

                List<Symptom> symptomsOfMedicalInformation = symptomManager.getSymptomsOfMedicalInformation(medicalInformation.getId());
                medicalInformation.setSymptoms(symptomsOfMedicalInformation);


                sendDataViaNetwork.sendMedicalInformation(medicalInformation);

                String response = recieveDataViaNetwork.receiveString();
                if(response.equals("RECEIVED MEDICAL INFORMATION")){
                    menuPaciente(patient, sendDataViaNetwork,recieveDataViaNetwork , socket, patientManager, symptomManager, medicalInformationManager);
                }

            }else{
                sendDataViaNetwork.sendStrings("ERROR");
            }
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error or client disconnected");
            releaseResources(recieveDataViaNetwork, sendDataViaNetwork, socket);
        }


    }

    private static void adminRegister(ReceiveDataViaNetwork recieveDataViaNetwork, SendDataViaNetwork sendDataViaNetwork, Socket socket, AdministratorManager adminManager, UserManager userManager) throws IOException {
        try {
            String message = recieveDataViaNetwork.receiveString();
            System.out.println(message);

            if (message.equals("OK")) {
                System.out.println("Registering administrator...");
                Administrator administrator = recieveDataViaNetwork.recieveAdmin();
                System.out.println(administrator.toString());
                User user = recieveDataViaNetwork.recieveUser();
                System.out.println(user.toString());
                adminManager.insertAdministrator(administrator); //Primero añadir al paciente a la DB

                int admin_id = adminManager.getAdministratorByEmail(administrator.getEmail()).getId(); //Obtener su ID
                System.out.println("Admin ID from email = " + admin_id);
                user.setAdmin_id(admin_id); //Asignar la Foreign Key al usuario
                userManager.addUser(user); //Añadir el usuario a la DB

                sendDataViaNetwork.sendStrings("SUCCESS");
                administrator.setId(admin_id);
                menuAdmin(administrator,sendDataViaNetwork, recieveDataViaNetwork, socket, adminManager);

            } else {
                System.out.println("Error in register");

            }
        } catch (IOException ex) {
            System.out.println("Error or client disconnected");
            releaseResources(recieveDataViaNetwork, sendDataViaNetwork, socket);
        }
    }

    private static void logInAdmin(ReceiveDataViaNetwork recieveDataViaNetwork, SendDataViaNetwork sendDataViaNetwork, Socket socket, AdministratorManager adminManager, UserManager userManager) throws IOException {
        try {
            sendDataViaNetwork.sendStrings("Administrator log in");
            String message = recieveDataViaNetwork.receiveString();
            System.out.println(message);
            Role role = new Role("Administrator");

            if (message.equals("OK")) {
                User user = recieveDataViaNetwork.recieveUser();
                boolean correctPassword = userManager.checkPassword(new String(user.getPassword()), user.getEmail());
                if (correctPassword) {
                    sendDataViaNetwork.sendStrings("SUCCESS");

                    int admin_id = adminManager.getAdministratorByEmail(user.getEmail()).getId();
                    Administrator administrator = adminManager.getAdministratorById(admin_id);
                    user.setAdmin_id(admin_id);

                    System.out.println(administrator.toString());

                    sendDataViaNetwork.sendAdmin(administrator);
                    //
                    menuAdmin(administrator, sendDataViaNetwork, recieveDataViaNetwork, socket, adminManager);
                } else {
                    sendDataViaNetwork.sendStrings("ERROR");
                }
            } else {
                System.out.println("Error in login");
            }


        } catch (IOException e) {
            System.out.println("Error or client disconnected");
            releaseResources(recieveDataViaNetwork, sendDataViaNetwork, socket);
        }
    }

    private static void AdminMenu(SendDataViaNetwork sendDataViaNetwork, ReceiveDataViaNetwork recieveDataViaNetwork, Socket socket, AdministratorManager administratorManager, JDBCUserManager userManager) throws IOException {
        try {
            boolean patientMenu = true;

            while (patientMenu) {
                int opcion = recieveDataViaNetwork.receiveInt();
                switch (opcion) {
                    case 1:
                        System.out.println("Administrator log in");
                        logInAdmin(recieveDataViaNetwork, sendDataViaNetwork, socket, administratorManager, userManager);
                        break;
                    case 2:
                        System.out.println("Administrator register");
                        adminRegister(recieveDataViaNetwork, sendDataViaNetwork, socket, administratorManager, userManager);
                        break;
                    case 3:
                        patientMenu = false;
                        System.out.println("Administrator disconnected");
                        break;
                    default:
                        System.out.println("Invalid option");
                        break;
                }
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            releaseResources(recieveDataViaNetwork, sendDataViaNetwork, socket);
        }
    }

    private static void menuAdmin(Administrator administrator, SendDataViaNetwork sendDataViaNetwork, ReceiveDataViaNetwork recieveDataViaNetwork, Socket socket, AdministratorManager administratorManager) throws IOException {
        try {
            boolean adminMenu= true;

            while (adminMenu) {
                int opcion = recieveDataViaNetwork.receiveInt();
                switch (opcion) {
                    case 1:
                        System.out.println("Close Server");
                        break;
                    case 0:
                        System.out.println("0. Exit");
                        break;
                    default:
                        System.out.println("Invalid option");
                        break;
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            releaseResources(recieveDataViaNetwork, sendDataViaNetwork, socket);
        }


    }


    // En Main.java

    private static void patientReceiveAndSaveSignal(Patient patient, ReceiveDataViaNetwork receiveData, SendDataViaNetwork sendData) {
        ConnectionManager conMan = null;
        try {
            Signal signal = receiveData.receiveSignal();

            if (signal != null) {
                signal.setClientId(patient.getId());

                // 1. Guardar archivo de texto (.txt)
                String generatedFileName = saveSignalToFile(signal, patient);

                // --- NUEVO: 2. Generar imagen gráfica (.png) ---
                generateSignalGraph(signal, generatedFileName);
                // ----------------------------------------------

                // 3. Guardar en Base de Datos (usando el nombre del txt o ambos)
                conMan = new ConnectionManager();
                JDBCSignalManager signalManager = new JDBCSignalManager(conMan);

                java.sql.Date date = new java.sql.Date(System.currentTimeMillis());

                // En la BBDD guardamos el nombre base. El doctor sabrá que existen .txt y .png
                signalManager.addSignal(signal, generatedFileName, date);

                System.out.println("Signal saved in DB, TXT and PNG.");
                sendData.sendStrings("OK");

            } else {
                sendData.sendStrings("ERROR");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conMan != null) conMan.close();
        }
    }

    private static String saveSignalToFile(Signal signal, Patient patient) {
        // Carpeta donde el servidor guardará los ficheros
        String directoryName = "ServerSignals";
        java.io.File directory = new java.io.File(directoryName);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Generar nombre de archivo: Signal_PatientID_Type_Timestamp.txt
        //Poner para que guarde bien la fecha de registro en el nombre del file
        long timestamp = System.currentTimeMillis();
        String fileName = "Signal_" + patient.getId() + "_" + signal.getType() + "_" + timestamp + ".txt";

        java.io.File file = new java.io.File(directoryName + "/" + fileName);

        try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
            // Escribir cabecera
            writer.println("Patient ID: " + patient.getId());
            writer.println("Signal Type: " + signal.getType());
            writer.println("Date: " + new java.util.Date(timestamp));
            writer.println("Values Count: " + signal.getValues().size());
            writer.println("----- BEGIN DATA -----");

            // Escribir los valores
            for (Integer val : signal.getValues()) {
                writer.println(val);
            }

            System.out.println("File saved at: " + file.getAbsolutePath());

        } catch (java.io.FileNotFoundException e) {
            System.err.println("Error writing signal file: " + e.getMessage());
        }
        return fileName;
    }

    private static void generateSignalGraph(Signal signal, String txtFileName) {
        int width = 800;   // Ancho de la imagen
        int height = 600;  // Alto de la imagen
        int padding = 50;  // Margen blanco alrededor

        // 1. Crear el lienzo (Canvas)
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();

        // 2. Fondo blanco
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, width, height);

        // 3. Dibujar ejes (Negro)
        g2.setColor(Color.BLACK);
        g2.drawLine(padding, height - padding, width - padding, height - padding); // Eje X
        g2.drawLine(padding, padding, padding, height - padding); // Eje Y

        // 4. Dibujar línea base (512 es el centro en BITalino de 10 bits)
        g2.setColor(Color.LIGHT_GRAY);
        int baselineY = height - padding - (int) ((512.0 / 1023.0) * (height - 2 * padding));
        g2.drawLine(padding, baselineY, width - padding, baselineY);
        g2.drawString("Base (512)", padding + 5, baselineY - 5);

        // 5. Configurar escalas
        List<Integer> values = signal.getValues();
        if (values.isEmpty()) return;

        double xScale = (double) (width - 2 * padding) / (values.size() - 1);
        // BITalino va de 0 a 1023. Escalamos para que quepa en la altura.
        double yScale = (double) (height - 2 * padding) / 1023.0;

        // 6. Dibujar la señal (Azul)
        g2.setColor(Color.BLUE);

        for (int i = 0; i < values.size() - 1; i++) {
            int x1 = padding + (int) (i * xScale);
            int y1 = height - padding - (int) (values.get(i) * yScale);

            int x2 = padding + (int) ((i + 1) * xScale);
            int y2 = height - padding - (int) (values.get(i + 1) * yScale);

            g2.drawLine(x1, y1, x2, y2);
        }

        // 7. Añadir texto informativo
        g2.setColor(Color.BLACK);
        g2.drawString("Patient: " + signal.getClientId() + " | Type: " + signal.getType(), padding, padding - 20);

        // 8. Guardar la imagen
        // Usamos el mismo nombre que el .txt pero cambiamos la extensión a .png
        String imageFileName = txtFileName.replace(".txt", ".png");
        // Asegúrate de usar la misma carpeta "ServerSignals"
        File outputFile = new File("ServerSignals/" + imageFileName);

        try {
            ImageIO.write(image, "png", outputFile);
            System.out.println("Gráfica generada: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error al guardar la imagen: " + e.getMessage());
        }

        g2.dispose(); // Liberar recursos gráficos
    }


}

