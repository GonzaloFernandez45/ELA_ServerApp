package ReceiveData;

import interfaces.*;
import jdbc.*;
import pojos.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.sql.Date;
import java.util.List;

public class ServerControl {

    private Socket socket;
    private ReceiveDataViaNetwork receiveDataViaNetwork;
    private SendDataViaNetwork sendDataViaNetwork;

    private PatientManager patientManager;
    private JDBCUserManager userManager;
    private SymptomManager symptomManager;
    private MedicalInformationManager medicalInformationManager;
    private DoctorManager doctorManager;
    private AdministratorManager administratorManager;

    public ServerControl(Socket socket, ReceiveDataViaNetwork receiveDataViaNetwork, SendDataViaNetwork sendDataViaNetwork, PatientManager patientManager, JDBCUserManager userManager, SymptomManager symptomManager, MedicalInformationManager medicalInformationManager, DoctorManager doctorManager, AdministratorManager administratorManager) {
        this.socket = socket;
        this.receiveDataViaNetwork = receiveDataViaNetwork;
        this.sendDataViaNetwork = sendDataViaNetwork;
        this.patientManager = patientManager;
        this.userManager = userManager;
        this.symptomManager = symptomManager;
        this.medicalInformationManager = medicalInformationManager;
        this.doctorManager = doctorManager;
        this.administratorManager = administratorManager;
    }

    /**
     * Primer punto de entrada: lee el primer int (1=Patient, 2=Doctor, 3=Admin)
     * y redirige al menú correspondiente.
     */
    public void handleFirstMessage() throws IOException {
        int message = receiveDataViaNetwork.receiveInt();
        System.out.println("First message from client: " + message);

        if (message == 1) {
            sendDataViaNetwork.sendStrings("PATIENT");
            patientMenu();
        } else if (message == 2) {
            sendDataViaNetwork.sendStrings("DOCTOR");
            doctorMenu();
        } else if (message == 3) {
            sendDataViaNetwork.sendStrings("ADMIN");
            adminMenu();
        } else {
            System.out.println("Unknown first message: " + message);
        }
    }

    private void patientMenu() throws IOException {
        try {
            boolean patientMenu = true;

            while (patientMenu) {
                int opcion = receiveDataViaNetwork.receiveInt();

                // Cliente desconectado
                if (opcion == -1) {
                    System.out.println("Patient menu: client disconnected (receiveInt = -1)");
                    patientMenu = false;
                    break;
                }

                switch (opcion) {
                    case 1:
                        logInPatient();
                        break;
                    case 2:
                        System.out.println("Patient register");
                        patientRegister();
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
            releaseResources(receiveDataViaNetwork, sendDataViaNetwork, socket);
        }
    }

    private void patientRegister() throws IOException {
        try {
            String message = receiveDataViaNetwork.receiveString();
            System.out.println(message);

            if (message.equals("OK")) {
                System.out.println("Registering patient...");
                Patient patient = receiveDataViaNetwork.recievePatient();
                System.out.println(patient.toString());
                User user = receiveDataViaNetwork.recieveUser();
                System.out.println(user.toString());
                patientManager.addPatient(patient); //Primero añadir al paciente a la DB

                int patient_id = patientManager.getPatientIDFromEmail(patient.getEmail()); //Obtener su ID
                System.out.println("Patient ID from email = " + patient_id);
                user.setPatient_id(patient_id); //Asignar la Foreign Key al usuario
                userManager.addUser(user); //Añadir el usuario a la DB

                sendDataViaNetwork.sendStrings("SUCCESS");

                patient.setId(patient_id);
                menuPaciente(patient);

            } else {
                System.out.println("Error in register");

            }
        } catch (IOException ex) {
            System.out.println("Error or client disconnected");
            releaseResources(receiveDataViaNetwork, sendDataViaNetwork, socket);
        }
    }

    private void menuPaciente(Patient patient) throws IOException {
        try {
            boolean patientMenu = true;

            while (patientMenu) {
                int opcion = receiveDataViaNetwork.receiveInt();

                if (opcion == -1) {
                    System.out.println("menuPaciente: client disconnected (receiveInt = -1)");
                    patientMenu = false;
                    break;
                }

                switch (opcion) {
                    case 1:
                        System.out.println("Option 1: Insert medical info");
                        patientInsertMedicalInformartion(patient);
                        break;

                    case 2:
                        System.out.println("Option 2: Receiving Signal...");
                        patientReceiveAndSaveSignal(patient,receiveDataViaNetwork,sendDataViaNetwork);
                        break;

                    case 3:
                        System.out.println("Option 3: Sending doctor feedback");
                        patientSeeDoctorFeedback(patient);
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
            releaseResources(receiveDataViaNetwork, sendDataViaNetwork, socket);
        }
    }


    private void logInPatient() throws IOException {
        try{
            sendDataViaNetwork.sendStrings("Patient log in");
            String message = receiveDataViaNetwork.receiveString();
            System.out.println(message);
            Role role = new Role("Patient");

            if (message.equals("OK")) {
                User user = receiveDataViaNetwork.recieveUser();
                boolean correctPassword = userManager.checkPassword(new String(user.getPassword()), user.getEmail());
                if (correctPassword) {
                    sendDataViaNetwork.sendStrings("SUCCESS");

                    int patient_id = patientManager.getPatientIDFromEmail(user.getEmail());
                    Patient patient = patientManager.getPatientbyId(patient_id);
                    user.setPatient_id(patient_id);

                    System.out.println(patient.toString());

                    sendDataViaNetwork.sendPatient(patient);
                    //
                    menuPaciente(patient);
                } else {
                    sendDataViaNetwork.sendStrings("ERROR");
                }
            } else {
                System.out.println("Error in login");
            }


        } catch (IOException e) {
            System.out.println("Error or client disconnected");
            releaseResources(receiveDataViaNetwork, sendDataViaNetwork, socket);
        }
    }

    private void releaseResources(ReceiveDataViaNetwork recieveDataViaNetwork, SendDataViaNetwork sendDataViaNetwork, Socket socket) {
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

    private void doctorMenu() throws IOException {
        try {
            boolean doctorMenu = true;

            while (doctorMenu) {
                int opcion = receiveDataViaNetwork.receiveInt();

                // Cliente desconectado
                if (opcion == -1) {
                    System.out.println("Doctor pre-login menu: client disconnected (receiveInt = -1)");
                    doctorMenu = false;
                    break;
                }

                switch (opcion) {
                    case 1:
                        System.out.println("Doctor Log in");
                        logInDoctor();
                        break;
                    case 2:
                        System.out.println("Doctor register");
                        doctorRegister();
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
            releaseResources(receiveDataViaNetwork, sendDataViaNetwork, socket);
        }
    }

    private void doctorRegister() throws IOException {
        try {
            String message = receiveDataViaNetwork.receiveString();
            System.out.println(message);

            if (message.equals("OK")) {
                System.out.println("Registering Doctor...");
                Doctor doctor = receiveDataViaNetwork.receiveDoctor();
                System.out.println(doctor.toString());
                User user = receiveDataViaNetwork.recieveUser();
                System.out.println(user.toString());
                doctorManager.addDoctor(doctor); //Primero añadir al paciente a la DB

                int doctor_id = doctorManager.getDoctorIDFromEmail(doctor.getEmail()); //Obtener su ID

                user.setDoctor_id(doctor_id); //Asignar la Foreign Key al usuario
                userManager.addUser(user); //Añadir el usuario a la DB

                sendDataViaNetwork.sendStrings("SUCCESS");

                doctor.setId(doctor_id);

                System.out.println("Doctor requested patient list");;
                Patient patient = selectPatientForDoctor(doctor);

                menuDoctor(patient, doctor);

            } else {
                System.out.println("Error in register");

            }
        } catch (IOException ex) {
            System.out.println("Error or client disconnected");
            releaseResources(receiveDataViaNetwork, sendDataViaNetwork, socket);
        }
    }
    private Patient selectPatientForDoctor(Doctor doctor
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
        sendDataViaNetwork.sendStrings(sb.toString());

        // 3. Recibir el ID seleccionado por el doctor
        int selectedId = receiveDataViaNetwork.receiveInt();

        System.out.println("Selecting patient...");
        // 4. Buscar paciente
        Patient selected = patientManager.getPatientbyId(selectedId);
        System.out.println(selected);

        if (selected != null) {
            System.out.println("Doctor selected patient: " + selected.getName());
            return selected;
        } else {
            sendDataViaNetwork.sendStrings("ERROR: Patient not found");
        }
        return selected;
    }

    private void logInDoctor() throws IOException {
        try{
            sendDataViaNetwork.sendStrings("Doctor log in");
            String message = receiveDataViaNetwork.receiveString();
            System.out.println(message);
            Role role = new Role("Doctor");

            if(message.equals("OK")){
                User user = receiveDataViaNetwork.recieveUser();
                boolean correctPassword = userManager.checkPassword(new String(user.getPassword()), user.getEmail());
                if(correctPassword){
                    sendDataViaNetwork.sendStrings("SUCCESS");

                    int doctor_id = doctorManager.getDoctorIDFromEmail(user.getEmail());
                    Doctor doctor = doctorManager.getDoctorbyId(doctor_id);
                    user.setDoctor_id(doctor_id);
                    System.out.println(doctor.toString());
                    sendDataViaNetwork.sendDoctor(doctor);

                    System.out.println("Doctor requested patient list");;
                    Patient patient = selectPatientForDoctor(doctor);


                    menuDoctor(patient, doctor);

                }else{
                    sendDataViaNetwork.sendStrings("ERROR");}
            }else{
                System.out.println("Error in login");
            }


        } catch (IOException e) {
            System.out.println("Error or client disconnected");
            releaseResources(receiveDataViaNetwork,sendDataViaNetwork,socket);
        }
    }

    private void menuDoctor(Patient patient, Doctor doctor) throws IOException {
        try{

            boolean doctorMenu = true;
            while (doctorMenu) {
                int opcion = receiveDataViaNetwork.receiveInt();

                if (opcion == -1) {
                    System.out.println("menuDoctor: client disconnected (receiveInt = -1)");
                    doctorMenu = false;
                    break;
                }

                switch (opcion) {
                    case 1:
                        System.out.println("SELECTED: View patient details");
                        viewPatient(patient);
                        doctorViewMedicalInformation(patient,doctor);
                        break;
                    case 2:
                        System.out.println("SELECTED: Add feedback");
                        //addFeedback(socket, recieveDataViaNetwork,sendDataViaNetwork);
                        selectAndUpdateFeedback();
                        break;
                    case 3:
                        System.out.println("SELECTED: View recorded signal");
                        doctorViewSignals();
                        break;
                    case 4:
                        System.out.println("SELECTED: Change patient data");
                        updatePatientData();
                        break;
                    case 0:
                        System.out.println("0. Exit");
                        doctorMenu = false;
                        break;
                    default:
                        System.out.println("Invalid option");
                        break;
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            releaseResources(receiveDataViaNetwork, sendDataViaNetwork, socket);
        }
    }


    public void viewPatient(Patient patient) throws IOException {
        System.out.println("The doctor has requested the information of the patient:" + patient.getId());
        System.out.println(patient);
        if(patient != null){
            sendDataViaNetwork.sendPatient(patient);
        }
        else {
            sendDataViaNetwork.sendStrings("ERROR - PATIENT NOT FOUND");
        }

    }

    public void viewPatientMedInfo() throws IOException {
        int patient_id = receiveDataViaNetwork.receiveInt();
        System.out.println("The doctor has requested the Medical information of the patient:");
        ConnectionManager connectionManager = new ConnectionManager();
        JDBCMedicalInformationManager medicalInformationManager = new JDBCMedicalInformationManager(connectionManager);
        List<MedicalInformation> medicalInformationList = medicalInformationManager.getMedicalInfoByPatientId(patient_id);
        sendDataViaNetwork.sendMedicalInformationList(medicalInformationList);
    }

    public void selectAndUpdateFeedback() throws IOException {

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


    public void addFeedback() throws IOException {
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


    public void updatePatientData() throws IOException {
        int patientId = receiveDataViaNetwork.receiveInt();

        String newName = receiveDataViaNetwork.receiveString();
        String newSurname = receiveDataViaNetwork.receiveString();
        int newPhone = receiveDataViaNetwork.receiveInt();
        String newEmail = receiveDataViaNetwork.receiveString();
        String newdni = receiveDataViaNetwork.receiveString();
        String newSex = receiveDataViaNetwork.receiveString();
        int newInsurance = receiveDataViaNetwork.receiveInt();

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
            sendDataViaNetwork.sendStrings("Patient data updated successfully.");
        } else {
            sendDataViaNetwork.sendStrings("No data updated.");
        }
    }


    private void patientInsertMedicalInformartion(Patient patient) throws IOException {
        try {
            String message = receiveDataViaNetwork.receiveString();
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

                MedicalInformation medicalInformation = receiveDataViaNetwork.receiveMedicalInformation();
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

                } else {
                    sendDataViaNetwork.sendStrings("ERROR");
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error or client disconnected");
            releaseResources(receiveDataViaNetwork, sendDataViaNetwork, socket);
        }



    }

    private void doctorViewMedicalInformation(Patient patient,Doctor doctor) throws IOException {
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

            if (response != null && response.equals("RECEIVED MEDICAL INFORMATION")) {
                System.out.println("Doctor confirmed reception of medical information.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendDataViaNetwork.sendStrings("ERROR: Could not fetch medical information");
        }
    }

    private void patientSeeDoctorFeedback(Patient patient) {

        try {
            String message = receiveDataViaNetwork.receiveString();

            if (message.equals("REQUEST FEEDBACK")) {
                System.out.println("Received request feedback, sending feedback");
                sendDataViaNetwork.sendStrings("OK");

                //buscar por fecha el medical report
                String dateString = receiveDataViaNetwork.receiveString();
                Date date = Date.valueOf(dateString);

                MedicalInformation medicalInformation = medicalInformationManager.getMedicalInformationByDate(date, patient.getId());
                //need to set symptoms and medication

                List<Symptom> symptomsOfMedicalInformation = symptomManager.getSymptomsOfMedicalInformation(medicalInformation.getId());
                medicalInformation.setSymptoms(symptomsOfMedicalInformation);


                sendDataViaNetwork.sendMedicalInformation(medicalInformation);

                String response = receiveDataViaNetwork.receiveString();
                if (response != null && response.equals("RECEIVED MEDICAL INFORMATION")) {
                    System.out.println("Patient confirmed reception of doctor feedback.");
                }

            }else{
                sendDataViaNetwork.sendStrings("ERROR");
            }
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error or client disconnected");
            releaseResources(receiveDataViaNetwork, sendDataViaNetwork, socket);
        }


    }


    private void adminRegister() throws IOException {
        try {
            String message = receiveDataViaNetwork.receiveString();
            System.out.println(message);

            if (message.equals("OK")) {
                System.out.println("Registering administrator...");
                Administrator administrator = receiveDataViaNetwork.recieveAdmin();
                System.out.println(administrator.toString());
                User user = receiveDataViaNetwork.recieveUser();
                System.out.println(user.toString());
                administratorManager.insertAdministrator(administrator); //Primero añadir al paciente a la DB

                int admin_id = administratorManager.getAdministratorByEmail(administrator.getEmail()).getId(); //Obtener su ID
                System.out.println("Admin ID from email = " + admin_id);
                user.setAdmin_id(admin_id); //Asignar la Foreign Key al usuario
                userManager.addUser(user); //Añadir el usuario a la DB

                sendDataViaNetwork.sendStrings("SUCCESS");
                administrator.setId(admin_id);
                menuAdmin(administrator);

            } else {
                System.out.println("Error in register");

            }
        } catch (IOException ex) {
            System.out.println("Error or client disconnected");
            releaseResources(receiveDataViaNetwork, sendDataViaNetwork, socket);
        }
    }

    private void logInAdmin() throws IOException {
        try {
            sendDataViaNetwork.sendStrings("Administrator log in");
            String message = receiveDataViaNetwork.receiveString();
            System.out.println(message);
            Role role = new Role("Administrator");

            if (message.equals("OK")) {
                User user = receiveDataViaNetwork.recieveUser();
                boolean correctPassword = userManager.checkPassword(new String(user.getPassword()), user.getEmail());
                if (correctPassword) {
                    sendDataViaNetwork.sendStrings("SUCCESS");

                    int admin_id = administratorManager.getAdministratorByEmail(user.getEmail()).getId();
                    Administrator administrator = administratorManager.getAdministratorById(admin_id);
                    user.setAdmin_id(admin_id);

                    System.out.println(administrator.toString());

                    sendDataViaNetwork.sendAdmin(administrator);
                    //
                    menuAdmin(administrator);
                } else {
                    sendDataViaNetwork.sendStrings("ERROR");
                }
            } else {
                System.out.println("Error in login");
            }


        } catch (IOException e) {
            System.out.println("Error or client disconnected");
            releaseResources(receiveDataViaNetwork, sendDataViaNetwork, socket);
        }
    }

    private void adminMenu() throws IOException {
        try {
            boolean patientMenu = true;

            while (patientMenu) {
                int opcion = receiveDataViaNetwork.receiveInt();

                if (opcion == -1) {
                    System.out.println("Admin menu: client disconnected (receiveInt = -1)");
                    patientMenu = false;
                    break;
                }

                switch (opcion) {
                    case 1:
                        System.out.println("Administrator log in");
                        logInAdmin();
                        break;
                    case 2:
                        System.out.println("Administrator register");
                        adminRegister();
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
            releaseResources(receiveDataViaNetwork, sendDataViaNetwork, socket);
        }
    }

    private void menuAdmin(Administrator administrator) throws IOException {
        try {
            boolean adminMenu= true;

            while (adminMenu) {
                int opcion = receiveDataViaNetwork.receiveInt();
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
            releaseResources(receiveDataViaNetwork, sendDataViaNetwork, socket);
        }


    }



    private static void patientReceiveAndSaveSignal(Patient patient, ReceiveDataViaNetwork receiveData, SendDataViaNetwork sendData) {
        ConnectionManager conMan = null;
        try {
            Signal signal = receiveData.receiveSignal();

            if (signal != null) {
                signal.setClientId(patient.getId());

                // --- YA NO GUARDAMOS ARCHIVO FÍSICO NI GENERAMOS GRÁFICA EN SERVIDOR ---
                // String generatedFileName = saveSignalToFile(signal, patient); // BORRAR
                // generateSignalGraph(signal, generatedFileName); // BORRAR OPCIONAL

                conMan = new ConnectionManager();
                JDBCSignalManager signalManager = new JDBCSignalManager(conMan);

                java.sql.Date date = new java.sql.Date(System.currentTimeMillis());

                // Pasamos "DB" como nombre de archivo porque ya no importa, los datos van dentro
                signalManager.addSignal(signal, "STORED_IN_DB", date);

                System.out.println("Signal saved in DB successfully.");
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

    private String saveSignalToFile(Signal signal, Patient patient) {
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

    private void generateSignalGraph(Signal signal, String txtFileName) {
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

    private void doctorViewSignals() throws IOException {
        ConnectionManager cm = null;
        try {
            // 1. Recibir ID del paciente
            int patientIdForSignal = receiveDataViaNetwork.receiveInt();

            // 2. Preparar conexión y Manager
            cm = new ConnectionManager();
            JDBCSignalManager signalManager = new JDBCSignalManager(cm);

            // 3. Obtener lista de señales
            List<Signal> signals = signalManager.listSignalsByPatientId(patientIdForSignal);

            // 4. Enviar lista al doctor (Metadatos)
            sendDataViaNetwork.sendSignalList(signals);

            // Si no hay señales, cerramos y salimos
            if (signals.isEmpty()) {
                System.out.println("No signals found for patient " + patientIdForSignal);
                return;
            }

            // 5. Esperar selección del doctor
            int selectedSignalId = receiveDataViaNetwork.receiveInt();

            if (selectedSignalId == -1) {
                System.out.println("Doctor cancelled signal view.");
                return;
            }

            System.out.println("Retrieving content for Signal ID: " + selectedSignalId);

            // 6. Recuperar señal completa (Leyendo el archivo .txt)
            Signal fullSignal = signalManager.getSignalWithValues(selectedSignalId);

            if (fullSignal != null) {
                // 7. Enviar señal completa (Datos)
                sendDataViaNetwork.sendSignal(fullSignal);
                System.out.println("Signal data sent successfully.");
            } else {
                System.out.println("Error: Signal file not found or corrupted.");
                // Opcional: Podrías enviar una señal vacía o manejar error en el cliente
            }

        } catch (Exception e) {
            System.err.println("Error handling signal view: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Aseguramos cerrar la conexión temporal
            if (cm != null) {
                cm.close();
            }
        }
    }

}
